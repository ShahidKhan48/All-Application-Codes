import Layout from "@/components/molecules/layout";
import ScreenHeader from "@/components/molecules/screen-header";
import WebView from "@/components/molecules/webView";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import Spinner from "@/components/ui/spinner";
import { fetchAgentOrderListingAsync } from "@/configs/requests/order-service";
import { useToast } from "@/hooks/use-toast";
import { dateFormat } from "@/lib/date";
import { getUserIds } from "@/lib/storage";
import { useQuery } from "@tanstack/react-query";
import {
  ChevronDown,
  ChevronUp,
  MoreHorizontal,
  Pencil,
  Plus,
} from "lucide-react";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const OrderCard = React.memo(
  ({
    order,
    idx,
    onEdit,
  }: {
    order: any;
    idx: number;
    onEdit: (orderId: any) => void;
  }) => {
    const [showItems, setShowItems] = useState<boolean>(false);
    const buyer = order.freeFlowEntityPartyDTOList?.find(
      (p) => p.partyType === 1
    );

    const expectedDeliveryDate =
      order.freeFlowEntityAdditionalInfoDTOList?.find(
        (i) => i.infoType === "expected_delivery_date"
      )?.value;

    const getStatusColor = (status: string) => {
      const colorMap = {
        DELIVERED: "bg-success text-white",
        ISSUED: "bg-warning",
        DRAFT: "bg-secondary text-black",
        CANCELLED: "bg-destructive",
      };
      return colorMap[status];
    };

    const toggleShowItems = () => {
      setShowItems((prev) => !prev);
    };

    const itemLists = order?.freeFlowEntityItemDTOList?.filter(
      (orderItem) => orderItem.itemTypeId === 1
    );

    return (
      <div
        key={order.id || order.orderId || idx}
        className={`relative border-l-4 border-primary border rounded-xl shadow-sm transition-shadow bg-white ${
          showItems ? "ring-2 ring-primary" : "hover:shadow-lg"
        } flex flex-col`}
      >
        <Card
          key={order.id || order.orderId || idx}
          className="p-4"
          data-testid={`d2c-order-card-${order.id || order.orderId || idx}`}
        >
          <div className="space-y-3">
            {/* Header with Order ID and Actions */}
            <div className="flex items-center justify-between flex-wrap-reverse">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-muted-foreground">
                  Order ID:
                </span>
                <span className="font-semibold">
                  {order.orderId || order.id}
                </span>
              </div>
              {["ISSUED", "DRAFT"].includes(
                order.entityStatus?.toUpperCase()
              ) && (
                <Button variant="link" onClick={() => onEdit(order.orderId)}>
                  <Pencil className="h-4 w-4 " />
                  Edit
                </Button>
              )}
            </div>

            {/* Dates */}
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="flex flex-col items-start justify-start">
                <span className="text-muted-foreground">Order Date:</span>
                <p>
                  {dateFormat(
                    order.createdOn || order.entityDate,
                    "dd-MMM-yyyy"
                  )}
                </p>
              </div>
              <div className="flex flex-col items-start justify-start">
                <span className="text-muted-foreground">Delivery Date:</span>
                <p>{dateFormat(expectedDeliveryDate, "dd-MMM-yyyy")}</p>
              </div>
            </div>

            {/* Amount and Buyer name */}
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="flex flex-col items-start justify-start">
                <span className="text-muted-foreground">Buyer:</span>
                <p>{buyer?.name || "-"}</p>
              </div>
              <div className="flex flex-col items-start justify-start">
                <span className="text-muted-foreground">Amount:</span>
                <p className="font-[600]">
                  ₹{order.totalAmount?.toLocaleString()}
                </p>
              </div>
            </div>

            <div className="flex flex-col items-start justify-start">
              <span className="text-muted-foreground">Status:</span>
              <Badge className={`${getStatusColor(order.entityStatus)}`}>
                {(order.entityStatus || "").toUpperCase()}
              </Badge>
            </div>

            <div
              onClick={toggleShowItems}
              className="flex items-center justify-center "
            >
              {!showItems && <ChevronDown />}
              {showItems && <ChevronUp />}
            </div>
            {showItems && (
              <div className="mt-4">
                <div className="font-semibold mb-2 text-start">Order Items</div>
                <table className="min-w-full border text-xs">
                  <thead>
                    <tr className="bg-muted">
                      <th className="border px-2 py-1">Item Name</th>
                      <th className="border px-2 py-1">Quantity</th>
                      <th className="border px-2 py-1">₹ Selling Price</th>
                      <th className="border px-2 py-1">₹ Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {itemLists?.map((item) => (
                      <tr key={item.itemId}>
                        <td className="border px-2 py-1">{item.itemName}</td>
                        <td className="border px-2 py-1">{item.quantity}</td>
                        <td className="border px-2 py-1">
                          {item.unitSellPrice}
                        </td>
                        <td className="border px-2 py-1">
                          {item.subLineTotal}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </Card>
      </div>
    );
  }
);

const OrderLists = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [fromDate, setFromDate] = useState("");
  const [editWebViewConfig, setEditWebViewConfig] = useState<{
    show: boolean;
    orderId: any;
  }>({} as any);

  const {
    data: orderListData,
    isLoading,
    isRefetching,
    isFetching,
    refetch,
  } = useQuery({
    queryKey: ["fetchAgentOrderListingAsync", fromDate],
    queryFn: async () => {
      const response = await fetchAgentOrderListingAsync({
        input: {
          from_date: fromDate,
          page: 0,
          size: 500,
          user_id: getUserIds()?.systemUserId,
        },
      });
      return response?.data;
    },
    enabled: !!fromDate,
  });

  const { data = [], total } = orderListData || {};
  const omniTradeUrl = import.meta.env.VITE_OMNI_WEBVIEW_URL;

  const handleWebViewClose = ({
    close,
    success,
  }: {
    close: boolean;
    success?: boolean;
  }) => {
    if (success === true) {
      toast({
        title: "Success",
        description: "Order Modified Successfully!",
      });
      refetch();
    }
    if (close === true) {
      setEditWebViewConfig({} as any);
    }
  };

  if (editWebViewConfig?.show === true && omniTradeUrl) {
    return (
      <div className="min-h-full bg-background" style={{ zIndex: 999 }}>
        {/* Header */}
        <ScreenHeader
          title="Edit Order"
          onBack={() => setEditWebViewConfig({} as any)}
        />
        <WebView
          src={`${omniTradeUrl}/delivery-orders/edit/${editWebViewConfig.orderId}`}
          onClose={handleWebViewClose}
        />
      </div>
    );
  }

  const handleCreateTaskClick = () => {
    navigate("/store-lists");
  };

  return (
    <Layout
      headerContent={
        <ScreenHeader
          title="Order Lists"
          onBack={() => navigate("/dashboard")}
        />
      }
      footerContent={
        <div className="flex items-center justify-center">
          <Button size="mobile" onClick={handleCreateTaskClick}>
            <Plus className="w-4 h-4 mr-2" />
            Add New Order
          </Button>
        </div>
      }
    >
      <div className="flex items-start flex-col gap-2">
        <Label htmlFor="deliveryDate">From Date</Label>
        <Input
          id="deliveryDate"
          type="date"
          value={fromDate}
          onChange={(e) => setFromDate(e.target.value)}
          className="w-auto"
        />
      </div>
      {!fromDate && data?.length <= 0 && (
        <div className="text-center font-[600] text-lg">
          Please select from date to proceed
        </div>
      )}
      {total > 0 && (
        <div className="flex items-start font-[600] text-lg">
          Total Orders: ({total})
        </div>
      )}
      <div className="space-y-3 ">
        {isLoading || isRefetching || isFetching ? (
          <Spinner />
        ) : (
          <>
            {data.map((order, idx) => (
              <OrderCard
                key={order.id || order.orderId || idx}
                idx={idx}
                order={order}
                onEdit={(orderId) =>
                  setEditWebViewConfig({
                    show: true,
                    orderId,
                  })
                }
              />
            ))}
          </>
        )}
      </div>
    </Layout>
  );
};
export default OrderLists;
