import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  ArrowLeft,
  Building,
  CheckCircle,
  MapPin,
  Phone,
  Plus,
  Trash2,
  User,
  X,
} from "lucide-react";
import { Product } from "@/data/mockData";
import { useToast } from "@/hooks/use-toast";
import {
  createWholeSaleOrderAsync,
  fetchInventorySellerCatlougeAsync,
  fetchSellerProductCatalogsAsync,
  searchInventoryUserAsync,
  searchInventoryUserByAcessAsync,
} from "@/configs/requests/collection-service";
import UnavailableItemQtyView from "@/components/ui/unavilable-item-view";
import { getUserIds, getUserRealmInfo } from "@/lib/storage";
import { format } from "date-fns";
import { ItemTypeahead } from "@/components/ui/item-type-head";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useTaskStore } from "@/hooks/useTasks";
import { Address, IAddress, ISearchBusinessRelationsbyAccess, Realm, Relation } from "@/lib/common-type";
import { closeAgentTaskAsync } from "@/configs/requests/dashbord-service";
import Layout from "@/components/molecules/layout";

interface OrderItem {
  product: Product;
  quantity: number;
  price: number;
  total: number;
}

const SalesOrder = () => {
  const selectedTask = useTaskStore((state) => state.selectedTask);
  const selectedStore = useTaskStore((state) => state.slectedStoreInfo);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [reference, setReference] = useState("");
  const [orderDate, setOrderDate] = useState("");
  const [expectedShipmentDate, setExpectedShipmentDate] = useState("");
  const [paymentTerms, setPaymentTerms] = useState("Due on Receipt");
  const [items, setItems] = useState([]);
  const [catalog, setCatalog] = useState([]);
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [selectedWarehouse, setSelectedWarehouse] = useState<Relation>(null);
  const [selectedCustomer, setSelectedCustomer] = useState<Relation>(null);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [orderError, setOrderError] = useState("");
  const [customerNotes, setCustomerNotes] = useState("");
  const [terms, setTerms] = useState("");

  const navigate = useNavigate();
  const { toast } = useToast();

  // Set orderDate to current date on mount
  useEffect(() => {
    if (!orderDate) {
      const today = new Date();
      const yyyy = today.getFullYear();
      const mm = String(today.getMonth() + 1).padStart(2, "0");
      const dd = String(today.getDate()).padStart(2, "0");
      setOrderDate(`${yyyy}-${mm}-${dd}`);
    }
  }, []);

  // Set expectedShipmentDate to current date on mount
  useEffect(() => {
    if (!expectedShipmentDate) {
      const today = new Date();
      const yyyy = today.getFullYear();
      const mm = String(today.getMonth() + 1).padStart(2, "0");
      const dd = String(today.getDate()).padStart(2, "0");
      setExpectedShipmentDate(`${yyyy}-${mm}-${dd}`);
    }
  }, []);

  // Fetch items for selected source warehouse
  useEffect(() => {
    if (!selectedWarehouse || !selectedWarehouse?.userId) {
      setCatalog([]);
      setCatalogLoading(false);
      return;
    }
    const wh = selectedWarehouse;
    const systemUserId = wh?.userId;
    // Extract businessUnit UUID from additionalDetails
    const businessUnit = wh?.businessUnits?.[0]?.businessUnit
    setCatalogLoading(true);
    fetchInventorySellerCatlougeAsync(businessUnit, String(systemUserId))
      .then((res) => {
        const userId = String(systemUserId);
        const catalogs = res?.data?.[userId]?.productCatalogs || [];
        setCatalog(catalogs);
      })
      .catch((err) => {
        setCatalog([]);
        // console.error('[ERROR] Fetching catalog:', err);
      })
      .finally(() => setCatalogLoading(false));
  }, [selectedWarehouse?.userId]);

  useEffect(() => {
    // Helper to fetch inventory users by tag using the API

    const fetchInventoryCustomer = async () => {
      //.id and systemUserId are optional for the API (API will fallback to storage if not provided)
      const result : ISearchBusinessRelationsbyAccess = await searchInventoryUserByAcessAsync({
        input: {
          businessRelationsFilter: {
            userTypes: ["CUSTOMER"],
            searchTerm: String(selectedStore?.store_id),
            pageNo: 0,
            pageSize: 1000,
          },
          fetchRequestType: "CUSTOMER_FILTER_REQUEST_V2",
        },
      });
      // The API returns the full response, so extract .data.data for compatibility
      if (result) {
        console.log(result.data?.[0], "fetchInventoryCustomer *******");
        setSelectedCustomer(result.data?.[0]);
      }
    };

    const fetchInventoryUser = async () => {
      const result : ISearchBusinessRelationsbyAccess = await searchInventoryUserAsync({
        input: {
          businessRelationsFilter: {
            pageNo: 0,
            pageSize: 50,
            userTypes: ["WAREHOUSE"],
            searchTerm: String(selectedTask?.warehouse_id),
          },
          fetchRequestType : 'WAREHOUSE_FILTER_REQUEST_V2'
        },
      });
      console.log(result?.data?.[0], "fetchInventoryUser *********");
      if (result) {
        setSelectedWarehouse(result?.data?.[0]);
      }
    };
    if (selectedStore && selectedTask) {
      fetchInventoryCustomer();
      fetchInventoryUser();
    }
  }, [selectedStore, selectedTask]);

  const handleAddItem = () => {
    if (!selectedWarehouse || !selectedWarehouse?.userId) {
      if (typeof toast === "function") {
        toast({
          title: "Error",
          description: "Please select a warehouse first",
          variant: "destructive",
        });
      }
      return;
    }
    setItems([
      ...items,
      {
        product: "",
        mrp: "",
        offerPrice: "",
        quantity: 1,
        commission: "",
        rateWithoutGst: "",
        rateWithGst: "",
        discount: "",
        tax: "",
        cess: "",
        amount: "",
        parentId: "",
        id: "",
        itemName: "",
        itemUomName: "",
        hsnCode: "",
        cessPercentage: "0",
        itemId: "",
      },
    ]);
  };
  const handleRemoveItem = (idx) => {
    setItems(items.filter((_, i) => i !== idx));
  };
  const taxOptions = [0, 3, 5, 10, 12, 15, 18, 28, 40];

  const handleItemChange = (idx, field, value) => {
    setItems(
      items.map((item, i) => {
        if (i !== idx) return item;
        // On item select, auto-fill all fields
        if (field === "product") {
          const selected = catalog.find(
            (c) => c.ProductCatalog.parentId === value
          );
          if (selected) {
            const pc = selected.ProductCatalog;
            const commission = selectedCustomer?.commission || 0;
            const offerPrice = pc?.price?.minimumPrice || 0;
            const mrp = pc?.mrp?.value || pc?.price?.minimumPrice || 0;
            const rateWithGst = (
              offerPrice -
              (offerPrice * (commission || 0)) / 100
            )?.toFixed(2);

            const amount = (
              offerPrice -
              (offerPrice * (commission || 0)) / 100
            )?.toFixed(2);

            const productTaxItemValue = pc.taxes?.find(
              (tax) => tax.type === "SGST"
            )?.value;

            const cess = pc.taxes?.find((tax) => tax.type === "CESS")?.value;
            const quantity = 1;

            const discount = "0";

            const rateWithoutGst = (
              productTaxItemValue !== undefined
                ? offerPrice - (offerPrice * Number(productTaxItemValue)) / 100
                : offerPrice
            )?.toFixed(2);

            return {
              ...item,
              product: value,
              mrp: pc.mrp?.value != null ? String(pc.mrp.value) : "",
              offerPrice: String(offerPrice),
              quantity: quantity,
              cess,
              discount,
              commission,
              rateWithoutGst: String(rateWithoutGst),
              rateWithGst: String(rateWithGst),
              amount: String(amount),
              tax: productTaxItemValue,
              id: pc.id,
              itemName: pc.descriptor?.name,
              itemUomName: pc.uom?.base,
              hsnCode: pc.hsnCode,
              cessPercentage: cess,
              itemId: pc.id,
              parentId: pc.parentId,
            };
          }
        }
        // On change of offerPrice, quantity, discount, tax, cess, recalculate
        let newItem = { ...item, [field]: value };
        const offerPrice = Number(newItem.offerPrice) || 0;
        const quantity = Number(newItem.quantity) || 0;
        const discount = Number(newItem.discount) || 0;
        const tax = Number(newItem.tax) || 0;
        const cess = Number(newItem.cess) || 0;
        const commission = Number(newItem.commission) || 0;
        const rateWithoutGst = (
          offerPrice -
          (offerPrice * Number(newItem.tax)) / 100
        )?.toFixed(2);
        const rateWithGst = (
          offerPrice -
          (offerPrice * Number(commission || 0)) / 100
        )?.toFixed(2);
        const amount = Number(rateWithGst) * quantity - discount;
        newItem = {
          ...newItem,
          quantity,
          rateWithoutGst: String(rateWithoutGst),
          rateWithGst: String(rateWithGst),
          amount: String(amount),
          commission: String(commission),
          tax: String(tax),
        };
        return newItem;
      })
    );
  };
  // Calculation helpers for summary (matches UI screenshots)
  const calcSubTotal = () =>
    items.reduce(
      (sum, item) => sum + Number(item.offerPrice) * Number(item.quantity),
      0
    );
  const calcDiscount = () =>
    items.reduce((sum, item) => sum + (Number(item.discount) || 0), 0);
  const calcBase = (item) => {
    const offerPrice = Number(item.offerPrice) || 0;
    const quantity = Number(item.quantity) || 0;
    const discount = Number(item.discount) || 0;
    const tax = Number(item.tax) || 0;
    const cessPercent = Number(item.cess) || 0;
    const totalTax = (tax + cessPercent) / 100;
    return (offerPrice * quantity - discount) / (1 + totalTax);
  };
  const calcTax = () =>
    items.reduce((sum, item) => {
      const base = calcBase(item);
      const tax = Number(item.tax) || 0;
      return sum + (base * tax) / 100;
    }, 0);
  const calcCESS = () =>
    items.reduce((sum, item) => {
      const base = calcBase(item);
      const cessPercent = Number(item.cess) || 0;
      return sum + (base * cessPercent) / 100;
    }, 0);
  const calcCommission = () => -calcDiscount();
  const calcTotal = () => calcSubTotal();

  const getUnavailableSelectedItems = async (
    systemUserId: string,
    selectedItems: []
  ) => {
    const res = await fetchSellerProductCatalogsAsync(systemUserId);
    const systemCatalogs = res?.data?.data || [];

    const unavailableProducts: [] = [];

    selectedItems?.forEach((item) => {
      const { id, quantity } = item || {};
      const systemProduct = systemCatalogs?.find(
        (systemCatalogItem) => systemCatalogItem.id === id
      );

      if (systemProduct) {
        const availableQty =
          systemProduct.quantity.value -
          (systemProduct.reservedQuantity?.value || 0);
        if (availableQty < parseFloat(quantity as string)) {
          const result = {
            ...item,
            availableQty,
            reservedQty: systemProduct.reservedQuantity?.value || 0,
          };
          unavailableProducts.push(result);
        }
      }
    });
    return unavailableProducts;
  };

  const getGstLabel = () => {
    if (selectedCustomer && selectedWarehouse) {
      const customerShipping = selectedCustomer?.shippingAddress
      const warehouseShipping =  selectedWarehouse?.shippingAddress
      const customerState = customerShipping?.state;
      const warehouseState = warehouseShipping?.state;
      if (customerState && warehouseState) {
        return customerState === warehouseState ? "GST %" : "IGST %";
      }
    }
    return "GST %";
  };

  const calculateCommission = (items) => {
    const totalSum = items.reduce((sum, item) => {
      if (item.itemTypeId === 1) {
        const mrpTotal = item.price.msp * item.quantity.count;
        const subLineTotal = item.subLineTotal || 0;
        return sum + (mrpTotal - subLineTotal);
      }
      return sum;
    }, 0);
    return Math.round(totalSum * 100) / 100;
  };

  const handlePlaceOrder = async () => {
    if (!selectedCustomer || !selectedWarehouse) {
      toast({
        title: "Error",
        description: "Please select both customer and warehouse",
        variant: "destructive",
      });
      return;
    }
    if (
      items.length === 0 ||
      calcTotal == 0 ||
      calcTotal == null ||
      calcTotal == undefined
    ) {
      toast({
        title: "Error",
        description: "Please add at least one item",
        variant: "destructive",
      });
      return;
    }
    const unavailableItems = await getUnavailableSelectedItems(
      selectedWarehouse.userId,
      items
    );

    if (unavailableItems?.length > 0) {
      toast({
        title: "Error",
        description: (
          <UnavailableItemQtyView unavailableItems={unavailableItems} />
        ),
        variant: "destructive",
      });
      return;
    }

    try {
      const itemsPayload = items.map((item) => {
        const isGST = getGstLabel() === "GST %";
        return {
          itemName: item.itemName,
          additionalInfo: JSON.stringify([
            { infoType: "hsn_code", value: item.hsnCode },
          ]),
          itemId: item.parentId,
          quantity: { count: item.quantity },
          price: {
            currency: "INR",
            mrp: String(item.mrp),
            msp: Number(item.offerPrice),
          },
          discount: Number(item.discount)
            ? Number(item.discount) * -1
            : undefined,
          gstPercentage: Number(item.tax) || 0,
          igstPercentage: isGST ? 0 : Number(item.tax) || 0,
          itemTypeId: 1,
          itemTypeName: "item",
          imageURL: "", // Add if available
          itemUomName: item.itemUomName,
          hsnCode: item.hsnCode,
          cessPercentage: Number(item.cess) || 0,
          subLineTotal:
            (item.rateWithGst || 0) * (item.quantity || 1) -
            Number(item.discount),
        };
      });

      if (selectedCustomer?.commission || selectedWarehouse?.commission) {
        itemsPayload.push({
          itemName: "commission",
          additionalInfo: "",
          itemId: "",
          quantity: { count: 1 },
          price: { currency: "INR", mrp: "0", msp: 0 },
          discount: 0,
          gstPercentage: 0,
          igstPercentage: 0,
          itemTypeId: 10,
          itemTypeName: "commission",
          imageURL: "",
          itemUomName: "",
          hsnCode: "",
          cessPercentage: 0,
          subLineTotal: calculateCommission(itemsPayload),
        });
      }

      let buyerName = "";
      let buyerContact = "";
      let buyerEmail = "";
      let buyerUserId = "";
      let buyerRealmId = "";
      try {
        const realmInfo = getUserRealmInfo();
        if (realmInfo) {
          buyerName = (realmInfo?.name as string) || "";
          buyerContact = (realmInfo?.primaryPhoneNumber as string) || "";
          buyerEmail = (realmInfo?.primaryEmailId as string) || "";
          buyerUserId = (realmInfo?.userId as string) || "";
          buyerRealmId = (realmInfo?.realmId as string) || "";
        }
      } catch (e) {
        console.error(e);
      }

      const partiesPayload = [
        {
          partyType: 2,
          name: selectedWarehouse?.name,
          userId: selectedWarehouse?.userId,
          contactNumber: selectedWarehouse?.contactNumber,
          gstIn: selectedWarehouse?.identityDetails?.gst?.gstNumber,
          realmId: selectedWarehouse?.userRealmId,
        },
        {
          partyType: 1,
          name: selectedCustomer?.name,
          userId: selectedCustomer?.userId,
          contactNumber: selectedCustomer?.contactNumber,
          gstIn: selectedCustomer?.identityDetails?.gst?.gstNumber,
          realmId: selectedCustomer?.userRealmId,
        },
        {
          name: buyerName,
          partyType: 3,
          contactNumber: buyerContact,
          email: buyerEmail,
          userId: buyerUserId,
          realmId: buyerRealmId,
        },
      ];

      // Address selection helpers

      const source_address = selectedCustomer?.shippingAddress;
      let shipping_address = null;
      let billing_address = null;
      if (
        selectedWarehouse
      ) {
        shipping_address =
          selectedWarehouse?.shippingAddress
        billing_address =
          selectedWarehouse?.billingAddress
      }
      // Use today's date for request_date and expected_delivery_date
      const today = new Date().toISOString().slice(0, 10);
      const requestDate = today;
      // Build payload as per working payload
      const payload = {
        input: {
          active: true,
          commission_percentage: selectedCustomer?.commission || 0,
          customer_notes: customerNotes || "",
          expected_delivery_date: expectedShipmentDate
            ? format(new Date(expectedShipmentDate), "yyyy-MM-dd")
            : "",
          items: itemsPayload,
          logged_in_user_id: getUserIds()?.userId,
          logged_in_user_type: "AGENT",
          parties: partiesPayload,
          payment_terms: paymentTerms,
          request_date: requestDate,
          source_address: source_address ? JSON.stringify(source_address) : "",
          shipping_address: shipping_address
            ? JSON.stringify(shipping_address)
            : "",
          billing_address: billing_address
            ? JSON.stringify(billing_address)
            : "",
          terms_and_conditions: terms || "",
        },
      };
      // Validation helpers

      const data = await createWholeSaleOrderAsync(payload);
      const closingRes = await closeAgentTaskAsync({
        input: {
          task_id: String(selectedTask?.id),
          additional_info: [
            {
              comments: "Task Completed",
            },
            {
              reason: "Task Completed",
            },
          ],
        },
      });
      if (closingRes) {
        navigate("/dashboard");
      }
    } catch (e) {
      toast({
        title: "Failed to place order",
        description: e.message,
        variant: "destructive",
      });
    }
  };

  useEffect(() => {
    console.log(items, "******* items");
  }, [items]);

  // Add these utility classes for input styling
  const inputNoSpinnerClass = "no-spinner";

  // Helper function to render address consistently
  const renderAddress = (entity, type: string) => {
    if (!entity || !Array.isArray(entity.address)) return "-";
    const address = entity.address.find((addr) => addr.name === type);
    if (!address) return "-";
    return `${address.locality || ""}, ${address.state || ""}, PIN: ${
      address.pincode || ""
    }`;
  };

  const rederWareHouseAddress = (address : Address) => {
    if (!address) return "-";
    return `${address.locality || ""}, ${address.state || ""}, PIN: ${
      address.pincode || ""
    }`;
  }

  return (
    <Layout
      headerContent={
        <div className="flex items-center gap-3 mb-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate(-1)}
            className="text-white hover:bg-white/20"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-lg font-semibold text-left">
              Create Sales Order
            </h1>
            <p className="text-white/80 text-sm text-left">
              {selectedCustomer?.name || selectedCustomer?.businessName}
            </p>
          </div>
        </div>
      }
      footerContent={
        <div className="flex justify-end gap-4 mt-6">
          <Button
            className="rounded-lg bg-gray-100 text-gray-700 px-6 py-3 font-semibold border border-gray-300 hover:bg-[#f97316] hover:text-white transition-colors"
            onClick={() => navigate(-1)}
          >
            Cancel
          </Button>
          <Button
            className="rounded-lg bg-blue-600 text-white px-6 py-3 font-semibold shadow"
            onClick={handlePlaceOrder}
            disabled={placingOrder}
          >
            {placingOrder ? "Placing Order..." : "Create"}
          </Button>
        </div>
      }
    >
      {/* Basic Info Card */}
      <Card>
        <CardHeader>
          <CardTitle className="text-left">Basic Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-y-4 gap-x-8">
            {/* <div>
              <label className="block text-black text-sm mb-1 font-medium">
                Reference#
              </label>
              <Input
                className="w-full border border-gray-300 rounded-lg px-4 py-3 bg-white text-lg"
                placeholder="Reference#"
                value={reference}
                onChange={(e) => setReference(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-black text-sm mb-1 font-medium">
                Payment Terms
              </label>
              <Input
                className="w-full border border-gray-300 rounded-lg px-4 py-3 bg-white text-lg"
                value={paymentTerms}
                onChange={(e) => setPaymentTerms(e.target.value)}
                placeholder="Payment Terms"
              />
            </div> */}
            <div>
              <label className="block text-black text-sm mb-1 font-medium text-left">
                Date*
              </label>
              <Input
                type="date"
                className="w-full border border-gray-300 rounded-lg px-4 py-3 bg-white text-lg"
                value={orderDate}
                onChange={(e) => setOrderDate(e.target.value)}
                required
                disabled
              />
            </div>
            <div>
              <label className="block text-black text-sm mb-1 font-medium text-left">
                Delivery date*
              </label>
              <Input
                type="date"
                className="w-full border border-gray-300 rounded-lg px-4 py-3 bg-white text-lg"
                value={expectedShipmentDate}
                onChange={(e) => setExpectedShipmentDate(e.target.value)}
                required
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Items Table Card */}
      <Card>
        <CardHeader>
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
            <CardTitle className="text-left">Items</CardTitle>
            <div className="flex items-center gap-4 flex-wrap md:ml-auto">
              <Button
                type="button"
                onClick={handleAddItem}
                size="sm"
                className="w-full"
              >
                <Plus className="h-4 w-4 mr-2" />
                Add Item
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {items.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No items added yet. Click "Add Item" to get started.
            </div>
          ) : (
            <div className="space-y-4">
              {items.map((item, idx) => (
                <div key={idx} className="border rounded-lg p-4 space-y-4">
                  <div className="flex items-center justify-between">
                    <h4 className="font-medium">Item {idx + 1}</h4>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveItem(idx)}
                      disabled={items.length === 1}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">
                        Item Detail
                      </label>
                      <ItemTypeahead
                        products={catalog.map((c) => ({
                          id: c.ProductCatalog.id,
                          itemName:
                            c.ProductCatalog.descriptor?.name || "Unnamed Item",
                          itemId: c.ProductCatalog.parentId,
                          unitPrice: c.ProductCatalog.price?.minimumPrice || 0,
                          gstPercentage:
                            c.ProductCatalog.taxes?.find(
                              (tax) => tax.type === "SGST"
                            )?.value || 0,
                          cessPercentage:
                            c.ProductCatalog.taxes?.find(
                              (tax) => tax.type === "CESS"
                            )?.value || 0,
                          hsnCode: c.ProductCatalog.hsnCode || "",
                          itemUomName: c.ProductCatalog.uom?.base || "",
                          imageURL:
                            c.ProductCatalog.descriptor?.media?.media?.[0]
                              ?.mediaUrl,
                          mrp: c.ProductCatalog.mrp?.value || 0,
                          rate: c.ProductCatalog.price?.minimumPrice || 0,
                          amount: c.ProductCatalog.price?.minimumPrice || 0,
                        }))}
                        selectedItem={(() => {
                          const selected = catalog.find(
                            (c) => c.ProductCatalog.parentId === item.product
                          );
                          if (!selected) return null;
                          return {
                            id: selected.ProductCatalog.id,
                            itemName:
                              selected.ProductCatalog.descriptor?.name ||
                              "Unnamed Item",
                            itemId: selected.ProductCatalog.parentId,
                            unitPrice:
                              selected.ProductCatalog.price?.minimumPrice || 0,
                            tax:
                              selected.ProductCatalog.taxes?.find(
                                (tax) => tax.type === "SGST"
                              )?.value || 0,
                            gstPercentage:
                              selected.ProductCatalog.taxes?.find(
                                (tax) => tax.type === "SGST"
                              )?.value || 0,
                            cessPercentage:
                              selected.ProductCatalog.taxes?.find(
                                (tax) => tax.type === "CESS"
                              )?.value || 0,
                            hsnCode: selected.ProductCatalog.hsnCode || "",
                            itemUomName:
                              selected.ProductCatalog.uom?.base || "",
                            imageURL:
                              selected.ProductCatalog.descriptor?.media
                                ?.media?.[0]?.mediaUrl,
                            mrp: selected.ProductCatalog.mrp?.value || 0,
                            rate:
                              selected.ProductCatalog.price?.minimumPrice || 0,
                            amount:
                              selected.ProductCatalog.price?.minimumPrice || 0,
                          };
                        })()}
                        placeholder={
                          catalogLoading ? "Loading..." : "Item Detail"
                        }
                        disabled={catalog.length === 0}
                        onSelect={(product) =>
                          handleItemChange(idx, "product", product.itemId)
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">MRP</label>
                      <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        value={item.mrp}
                        readOnly
                        className={inputNoSpinnerClass}
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">
                        Offer Price
                      </label>
                      <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        value={item.offerPrice}
                        onChange={(e) =>
                          handleItemChange(idx, "offerPrice", e.target.value)
                        }
                        className={inputNoSpinnerClass}
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">
                        Quantity
                      </label>
                      <Input
                        type="number"
                        min="1"
                        placeholder="0"
                        value={item.quantity}
                        onChange={(e) =>
                          handleItemChange(idx, "quantity", e.target.value)
                        }
                        className={inputNoSpinnerClass}
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">
                        Discount
                      </label>
                      <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        value={item.discount}
                        onChange={(e) =>
                          handleItemChange(idx, "discount", e.target.value)
                        }
                        className={inputNoSpinnerClass}
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">Tax</label>
                      <Select
                        value={item.tax}
                        onValueChange={(v) => handleItemChange(idx, "tax", v)}
                      >
                        <SelectTrigger className="w-full flex h-10 items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                          <SelectValue placeholder={`${getGstLabel()}%`} />
                        </SelectTrigger>
                        <SelectContent className="rounded-xl shadow-lg max-h-60 overflow-y-auto">
                          {taxOptions.map((opt) => (
                            <SelectItem key={opt} value={String(opt)}>
                              {opt}%
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">Cess</label>
                      <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        value={item.cess}
                        onChange={(e) =>
                          handleItemChange(idx, "cess", e.target.value)
                        }
                        className={inputNoSpinnerClass}
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="block text-sm font-medium">
                        Amount
                      </label>
                      <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        value={item.amount}
                        readOnly
                        disabled
                        className={inputNoSpinnerClass}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Summary Card - full width */}
      <Card className="w-full my-6">
        <CardHeader>
          <CardTitle className="text-left">Order Summary</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="flex justify-between mb-1">
            <span>Subtotal (incl. GST & CESS)</span>
            <span>₹{calcSubTotal().toFixed(2)}</span>
          </div>
          <div className="flex justify-between mb-1 text-xs text-muted-foreground text-left">
            <span>
              This subtotal is inclusive of all GST and CESS applied to items.
            </span>
          </div>
          <div className="flex justify-between mb-1">
            <span>
              Tax Breakdown
              {items[0]?.tax ? ` [${items[0].tax}%]` : ""}
            </span>
            <span>₹{calcTax().toFixed(2)}</span>
          </div>
          <div className="flex justify-between mb-1">
            <span>
              CESS Breakdown{items[0]?.cess ? ` [${items[0].cess}%]` : ""}
            </span>
            <span>₹{calcCESS().toFixed(2)}</span>
          </div>
          <div className="flex justify-between mb-1">
            <span>Commission</span>
            <span className="text-red-500">
              {calcCommission() < 0 ? "-" : ""}₹
              {Math.abs(calcCommission()).toFixed(2)}
            </span>
          </div>
          <hr className="my-2" />
          <div className="flex justify-between font-bold text-lg">
            <span>Total (incl. GST & CESS)</span>
            <span>₹{calcTotal().toFixed(2)}</span>
          </div>
          <div className="flex justify-between mb-1 text-xs text-muted-foreground">
            <span>
              This total is inclusive of all GST and CESS applied to items.
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Warehouse & Vendor Card */}
      <Card>
        <CardHeader>
          <CardTitle className="text-left">Warehouse & Customer</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6 gap-x-4 sm:gap-x-8  text-left">
            <div>
              {selectedWarehouse &&
                (() => {
                  const shipping =
                    selectedWarehouse?.shippingAddress
                  const billing =
                    selectedWarehouse?.billingAddress
                  const gstin = selectedWarehouse?.identityDetails?.gst?.gstNumber;
                  return (
                    <div className="text-xs text-muted-foreground mt-1 border rounded p-2 bg-gray-50 overflow-hidden">
                      {shipping && (
                        <div>
                          <b>Shipping Address:</b>
                          <br />
                          {shipping.locality}, {shipping.state},{" "}
                          {shipping.pincode}
                          {gstin && (
                            <div className="mt-1">
                              <b>GSTIN:</b> {gstin}
                            </div>
                          )}
                        </div>
                      )}
                      {billing && (
                        <div className="mt-1">
                          <b>Billing Address:</b>
                          <br />
                          {billing.locality}, {billing.state}, {billing.pincode}
                          {gstin && (
                            <div className="mt-1">
                              <b>GSTIN:</b> {gstin}
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })()}
            </div>
            <div>
              {selectedCustomer && 
                (() => {
                  const shipping =
                    selectedCustomer?.shippingAddress
                  const gstin = selectedCustomer?.identityDetails?.gst?.gstNumber;
                  return shipping ? (
                    <div className="text-xs text-muted-foreground mt-1 border rounded p-2 bg-gray-50 overflow-hidden">
                      <div>
                        <b>Shipping Address:</b>
                      </div>
                      <div>
                        {shipping.locality}, {shipping.state},{" "}
                        {shipping.pincode}
                      </div>
                      {gstin && (
                        <div className="mt-1">
                          <b>GSTIN:</b> {gstin}
                        </div>
                      )}
                    </div>
                  ) : null;
                })()}
            </div>
          </div>
        </CardContent>
      </Card>

      {(selectedCustomer || selectedWarehouse) && (
        <div className="mt-8">
          <div className="flex items-center gap-2 mb-6">
            <MapPin className="h-5 w-5 text-foreground" />
            <h4 className="text-xl font-medium text-foreground">
              Shipping & Billing Information
            </h4>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {selectedWarehouse && (
              <div className="rounded-xl border border-blue-200 bg-blue-50 p-4 space-y-2">
                <div className="flex items-center gap-2 mb-2">
                  <Building className="h-5 w-5 text-blue-600" />
                  <span className="text-base font-medium text-blue-900">
                    Ship From
                  </span>
                  <CheckCircle className="h-5 w-5 text-green-500 ml-auto" />
                </div>
                <div className="text-sm text-blue-900 whitespace-pre-line text-left">
                  {rederWareHouseAddress(selectedWarehouse?.shippingAddress)}
                </div>
                <div className="mt-2  text-left">
                  <Badge className="bg-white border border-blue-200 text-blue-900 text-xs px-2 py-1 font-semibold">
                    GSTIN: {selectedWarehouse?.identityDetails?.gst?.gstNumber || "-"}
                  </Badge>
                </div>
              </div>
            )}

            {selectedCustomer && (
              <>
                <div className="rounded-xl border border-green-200 bg-green-50 p-4 space-y-2">
                  <div className="flex items-center gap-2 mb-2">
                    <MapPin className="h-5 w-5 text-green-600" />
                    <span className="text-base font-medium text-green-900">
                      Ship To
                    </span>
                    <CheckCircle className="h-5 w-5 text-green-500 ml-auto" />
                  </div>
                  <div className="text-sm text-green-900 whitespace-pre-line text-left">
                    {rederWareHouseAddress(selectedCustomer?.shippingAddress)}
                  </div>
                  <div className="mt-2 text-left">
                    <Badge className="bg-white border border-green-200 text-green-900 text-xs px-2 py-1 font-semibold">
                      GSTIN: {selectedCustomer?.identityDetails?.gst?.gstNumber || "-"}
                    </Badge>
                  </div>
                </div>

                {selectedCustomer && (
                  <>
                    <div className="rounded-xl border border-green-200 bg-green-50 p-4 space-y-2">
                      <div className="flex items-center gap-2 mb-2">
                        <MapPin className="h-5 w-5 text-green-600" />
                        <span className="text-base font-medium text-green-900">
                          Ship To
                        </span>
                        <CheckCircle className="h-5 w-5 text-green-500 ml-auto" />
                      </div>
                      <div className="text-sm text-green-900 whitespace-pre-line text-left">
                        {rederWareHouseAddress(selectedCustomer?.shippingAddress)}
                      </div>
                      <div className="mt-2 text-left">
                        <Badge className="bg-white border border-green-200 text-green-900 text-xs px-2 py-1 font-semibold">
                          GSTIN: {selectedCustomer?.identityDetails?.gst?.gstNumber || "-"}
                        </Badge>
                      </div>
                    </div>

                    <div className="rounded-xl border border-orange-200 bg-orange-50 p-4 space-y-2">
                      <div className="flex items-center gap-2 mb-2">
                        <User className="h-5 w-5 text-orange-600" />
                        <span className="text-base font-medium text-orange-900">
                          Bill To
                        </span>
                        <CheckCircle className="h-5 w-5 text-green-500 ml-auto" />
                      </div>
                      <div className="text-sm text-orange-900 whitespace-pre-line text-left">
                        {rederWareHouseAddress(selectedCustomer?.billingAddress)}
                      </div>
                      <div className="mt-2 text-left">
                        <Badge className="bg-white border border-orange-200 text-orange-900 text-xs px-2 py-1 font-semibold">
                          GSTIN: {selectedCustomer?.identityDetails?.gst?.gstNumber || "-"}
                        </Badge>
                      </div>
                    </div>
                  </>
                )}
              </>
            )}

            {(selectedCustomer || selectedWarehouse) && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                {selectedWarehouse && (
                  <div className="space-y-2">
                    <h5 className="text-sm font-medium text-muted-foreground text-left">
                      Warehouse Contact
                    </h5>
                    <div className="space-y-1 text-left">
                      <p className="text-sm font-medium text-foreground">
                        {selectedWarehouse?.name || "-"}
                      </p>
                      {selectedWarehouse?.contactNumber
                        && (
                        <div className="flex items-center gap-1">
                          <Phone className="h-3 w-3 text-muted-foreground" />
                          <p className="text-sm text-muted-foreground">
                            {selectedWarehouse?.contactNumber
                               || "-"}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                )}
                {selectedCustomer && (
                  <div className="space-y-2">
                    <h5 className="text-sm font-medium text-muted-foreground text-left">
                      Vendor Contact
                    </h5>
                    <div className="space-y-1 text-left">
                      <p className="text-sm font-medium text-foreground">
                        {selectedCustomer?.name || "-"}
                      </p>
                      {selectedCustomer?.contactNumber && (
                        <div className="flex items-center gap-1">
                          <Phone className="h-3 w-3 text-muted-foreground" />
                          <p className="text-sm text-muted-foreground">
                            {selectedCustomer?.contactNumber || "-"}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Additional Information Card (Customer Notes & Terms) */}
      <Card className="w-full my-6">
        <CardHeader>
          <CardTitle className="text-left">Additional Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div>
            <label className="block text-sm mb-1 font-medium text-left">
              Customer Notes
            </label>
            <textarea
              className="form-control w-full border rounded p-2"
              placeholder="Will be displayed on wholesale order"
              value={customerNotes}
              onChange={(e) => setCustomerNotes(e.target.value)}
            />
          </div>
          <div>
            <label className="block  text-sm mb-1 font-medium text-left">
              Terms & Conditions
            </label>
            <textarea
              className="form-control w-full border rounded p-2"
              placeholder="Enter the terms and conditions of your business to be displayed in your transaction"
              value={terms}
              onChange={(e) => setTerms(e.target.value)}
            />
          </div>
        </CardContent>
      </Card>
    </Layout>
  );
};

export default SalesOrder;
