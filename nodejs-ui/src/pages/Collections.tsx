import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import {
  ArrowLeft,
  CreditCard,
  Plus,
  Smartphone,
  Building,
  Banknote,
  FileText,
  CheckCircle,
  ImageIcon,
  Upload,
  Pencil,
  Trash2,
} from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import {
  deleteCashTxnAsync,
  fetchCollectionByIdAsync,
  fetchStoreD2cOutstandingAsync,
  initiateAgentApprovalFlowAsync,
  searchInventoryUserByAcessAsync,
  updateCashTxnAsync,
} from '@/configs/requests/collection-service';
import { useTaskStore } from '@/hooks/useTasks';
import { IFetchStoreDTCAmt, INVENTORY_WAREHOUSE_TYPE, Relation } from '@/lib/common-type';
import WareHouseListPopupWithDropdown from '@/components/ui/warehouselist-dropdown-popup';
import { formatDateTime } from '@/lib/utils';
import EditCollectionModal from '@/components/ui/editCashTxnModal';
import AddCollectionDrawer from '@/components/ui/add-new-collection-drawer';
import { Collection } from '@/data/mockData';
import { closeAgentTaskAsync } from '@/configs/requests/dashbord-service';
import Layout from '@/components/molecules/layout';
import PaymentIcon from '@/components/molecules/payment-icon';
import { useToast } from '@/hooks/use-toast';
import ConfirmDialog from '@/components/ui/confirm';
import { QrDrawer } from '@/components/ui/qr_drawer';
import Spinner from '@/components/ui/spinner';

function generateTransactionId() {
  return crypto.randomUUID();
}

function CountdownTimer({ expiryTimestamp }: { expiryTimestamp: number }) {
  const [timeLeft, setTimeLeft] = useState(expiryTimestamp * 1000 - Date.now());

  useEffect(() => {
    const interval = setInterval(() => {
      setTimeLeft(expiryTimestamp * 1000 - Date.now());
    }, 1000);
    return () => clearInterval(interval);
  }, [expiryTimestamp]);

  if (timeLeft <= 0) {
    return (
      <p className="text-sm text-red-600 font-medium flex items-center gap-1">
        <span>⏰</span> Expired
      </p>
    );
  }

  const minutes = Math.floor((timeLeft / 1000 / 60) % 60);
  const seconds = Math.floor((timeLeft / 1000) % 60);

  return (
    <p className="text-sm text-red-600 font-medium flex items-center gap-1">
      <span>⏰</span> Expires in {minutes} minutes, {seconds} seconds
    </p>
  );
}

const Collections = () => {
  const navigate = useNavigate();
  const selectedTask = useTaskStore((state) => state.selectedTask);
  const selectedStore = useTaskStore((state) => state.slectedStoreInfo);
  const [qrData, setQrData] = useState({});
  const [showQr, setShowQr] = useState(false);
  const [kycStatus,setKycStatus] = useState(false);

  const { toast } = useToast();

  const {
    mutateAsync: fetchCollectionByIdMutate,
    isPending: isFetchingCollection,
  } = useMutation({
    mutationKey: ['fetchCollectionByIdAsync'],
    mutationFn: fetchCollectionByIdAsync,
  });

  const {
    mutateAsync: fetchStoreD2cOutstandingMutate,
    isPending: isFetchingOutStandingAmt,
  } = useMutation({
    mutationKey: ['fetchStoreD2cOutstandingAsync'],
    mutationFn: fetchStoreD2cOutstandingAsync,
  });

  const {
    mutateAsync: searchInventoryUserByAcessMutate,
    isPending: isFetchingInventory,
  } = useMutation({
    mutationKey: ['searchInventoryUserByAcessAsync'],
    mutationFn: searchInventoryUserByAcessAsync,
  });

  const { mutateAsync: updateCashMutate, isPending: isUpdating } = useMutation({
    mutationKey: ['updateCashTxnAsync'],
    mutationFn: updateCashTxnAsync,
  });

  const { mutateAsync: deleteCashMutate, isPending: isDeleting } = useMutation({
    mutationKey: ['deleteCashTxnAsync'],
    mutationFn: deleteCashTxnAsync,
  });

  const [collections, setCollections] = useState<Collection[]>([]);
  const [wareHouseList, setWareHouseList] = useState<
    Relation[]
  >([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newCollection, setNewCollection] = useState<Collection>();
  const [showWareHouseList, setShowWareHouseList] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [toggleTxn, setToogleTxn] = useState<Collection>();
  const [selectedWareHouse, setSelectedWareHouse] =
    useState<Relation>();
  const [orders, seletedOrders] = useState<IFetchStoreDTCAmt>();

  const getMethodColor = (method: Collection['payment_mode']) => {
    switch (method) {
      case 'CASH':
        return 'bg-success/10 text-success';
      case 'UPI':
        return 'bg-primary/10 text-primary';
      case 'NEFT':
        return 'bg-warning/10 text-warning';
      case 'CHEQUE':
        return 'bg-onboarding/10 text-onboarding';
      default:
        return 'bg-muted text-muted-foreground';
    }
  };

  const totalCollected = collections.reduce(
    (total, collection) => total + collection.amount,
    0
  );

  const cashCollected = collections
    .filter((item) => item.payment_mode === 'CASH')
    ?.reduce((total, collection) => total + collection.amount, 0);

  const onlineCollected = collections
    .filter(
      (item) =>
        item.payment_mode === 'ONLINE'
    )
    ?.reduce((total, collection) => total + collection.amount, 0);

  const chequeCollected = collections
    .filter((item) => item.payment_mode === 'CHEQUE')
    ?.reduce((total, collection) => total + collection.amount, 0);

  const fetchCollections = async () => {
    const data = await fetchCollectionByIdMutate({
      input: {
        task_id: selectedTask?.id,
        store_id: selectedStore?.store_id,
        fetch_kyc_info: true,
      },
    });
    console.log(data);
    setKycStatus(data?.kyc_status)
    setCollections(data?.collection_details);
  };

  useEffect(() => {
    if (selectedStore && selectedTask) {
      fetchCollections();
    }
  }, [selectedTask, selectedStore]);

  const searchInventoryUserByAcess = async () => {
    const data = await searchInventoryUserByAcessMutate({
      input: {
        fetchRequestType: 'WAREHOUSE_FILTER_REQUEST_V2',
        businessRelationsFilter: {
          pageNo: 0,
          pageSize: 50,
          searchTerm: '',
          userTypes: ['WAREHOUSE'],
        },
      },
    });
    const response = data?.data as Relation[];
    if (response?.length === 1) {
      onSelectOfWareHouse(response?.[0]);
    } else {
      setWareHouseList(response);
      setShowWareHouseList(true);
    }
  };

  const onSelectOfWareHouse = async (data: Relation) => {
    setShowWareHouseList(false);
    setSelectedWareHouse(data);
    const response = await fetchStoreD2cOutstandingMutate({
      input: {
        store_user_id: selectedStore?.store_id,
        fetch_order_info: true,
        warehouse_user_ids: [String(data?.userId)],
      },
    });
    if (response) {
      setShowAddForm(true);
      console.log(response);
      seletedOrders(response);
    }
  };

  const [deleteCollectionConfig, setDeleteCollectionConfig] = useState<{
    show: boolean;
    data: any;
  }>({} as any);

  const handleCollectionDeleteConfirm = (action: 'yes' | 'no') => {
    setDeleteCollectionConfig({} as any);
    if (action === 'no') return;
    if (!deleteCollectionConfig.data) return;
    const collection = deleteCollectionConfig.data;
    const { cash_transaction_id, amount, name } = collection || {};
    deleteCashMutate({
      input: {
        cashTransactionIds: [String(cash_transaction_id)],
        destinations: [{ destination: String(selectedStore?.contact_number) }],
        amount: String(amount),
        store_name: name,
      },
    }).then(() => {
      fetchCollections();
    });
  };

  const onEdit = (collection: Collection) => {
    if (
      collection?.payment_mode === 'CASH' ||
      collection?.payment_mode === 'NEFT' ||
      collection?.payment_mode === 'CHEQUE'
    ) {
      setShowEditModal(true);
      setToogleTxn(collection);
    }
  };

  const onUpdate = async (payload: Collection) => {
    console.log(payload, '****');
    const data = await updateCashMutate({
      input: {
        existing_cash_txn_id: String(payload?.cash_transaction_id),
        destinations: [{ destination: selectedStore?.contact_number }],
        amount: String(payload?.amount),
        store_name: selectedStore?.store_name,
        payment_type: payload?.payment_mode,
        utr_number: payload?.bank_reference_no,
        comments: payload?.comments,
        image_url: payload?.image_url,
        old_amount: String(toggleTxn?.amount),
      },
    });
    if (data) {
      fetchCollections();
    }
  };

  const triggerPaymentAsync = (txn: Collection) => {
    const payload = {
      input: {
        store_user_id: String(selectedStore?.store_id),
        task_id: String(selectedTask?.id),
        amount: txn?.amount,
        store: {
          user_id: String(selectedStore?.store_id),
          name: selectedStore?.store_name,
          realm_id: selectedStore?.store_realm_id,
          contact_number: selectedStore?.contact_number,
          business_sub_unit: selectedStore?.business_subunit,
        },
        payment_type: txn?.payment_mode,
        utr_number: txn?.bank_reference_no,
        transaction_id: generateTransactionId(),
        comments: txn?.comments,
        image_url: txn?.image_url,
        warehouse_user_ids: [String(selectedWareHouse?.userId)],
      },
    };
    initiateAgentApprovalFlowAsync(payload)
      .then((res) => {
        if (res?.qr_image_link) {
          setShowQr(true);
          setQrData({
            image: res?.qr_image_link,
            expiry: res?.link_expiry_in_mins,
          });
        }
        setShowAddForm(false);
        fetchCollections();
      })
      .catch((e) => {
        toast({
          title: 'Failed',
          description: e?.errorMessage || e?.message,
          variant: 'destructive',
        });
      });
  };

  const closeTaskAsync = async () => {
    const closingRes = await closeAgentTaskAsync({
      input: {
        task_id: String(selectedTask?.id),
        additional_info: [
          {
            comments: 'Task Completed',
          },
          {
            reason: 'Task Completed',
          },
        ],
      },
    });
    if (closingRes) {
      navigate('/dashboard');
    }
  };

  return (
    <Layout
      headerContent={
        <div className="flex items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate('/dashboard')}
            className="text-white hover:bg-white/20"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-lg font-semibold">Collections</h1>
            <p className="text-white/80 text-sm">Today's collections summary</p>
            <p className="text-white/80 text-sm">
              Store Name : {selectedStore?.store_name}
            </p>
          </div>
        </div>
      }
      footerContent={
        <div className="flex items-center justify-center">
          <Button size="mobile" onClick={searchInventoryUserByAcess}>
            <Plus className="w-4 h-4 mr-2" />
            Add New Collection
          </Button>
        </div>
      }
    >
      {isFetchingCollection ? (
        <Spinner label="Please wait while we are fetching details..." />
      ) : (
        <>
          {/* Summary Card */}
          <Card className="border-l-4 border-l-success">
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="w-full">
                  {/* 2x2 Grid */}
                  <div className="grid grid-cols-2 gap-4 mb-3">
                    {/* Cash Collected */}
                    <div>
                      <h3 className="text-lg font-semibold text-primary">
                        ₹{cashCollected.toLocaleString()}
                      </h3>
                      <p className="text-xs text-muted-foreground">
                        Cash Collected
                      </p>
                    </div>

                    {/* Online Collected */}
                    <div>
                      <h3 className="text-lg font-semibold text-blue-600">
                        ₹{onlineCollected.toLocaleString()}
                      </h3>
                      <p className="text-xs text-muted-foreground">
                        Online Collected
                      </p>
                    </div>

                    {/* Cheque Collected */}
                    <div>
                      <h3 className="text-lg font-semibold text-purple-600">
                        ₹{chequeCollected.toLocaleString()}
                      </h3>
                      <p className="text-xs text-muted-foreground">
                        Cheque Collected
                      </p>
                    </div>

                    {/* Total Collected */}
                    <div>
                      <h3 className="text-lg font-bold text-success">
                        ₹{totalCollected.toLocaleString()}
                      </h3>
                      <p className="text-xs text-muted-foreground">
                        Total Collected
                      </p>
                    </div>
                  </div>

                  {/* Transactions count */}
                  <p className="text-xs text-muted-foreground">
                    {collections.length} transactions
                  </p>
                </div>

                <CreditCard className="w-12 h-12 text-success ml-4" />
              </div>
            </CardContent>
          </Card>

          {/* Add Collection Button */}
          {selectedTask?.status !== 'COMPLETED' && (
            <Button
              size="mobile"
              className="w-full bg-red-500"
              onClick={closeTaskAsync}
            >
              Close Task
            </Button>
          )}

          {/* Collections List */}
          <div>
            <h2 className="text-xl font-semibold mb-4">Today's Collections</h2>
            {collections.length === 0 ? (
              <Card className="text-center p-8">
                <CreditCard className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-semibold mb-2">
                  No Collections Yet
                </h3>
                <p className="text-muted-foreground">
                  Start recording collections for today.
                </p>
              </Card>
            ) : (
              <div className="space-y-3">
                {collections.map((collection) => (
                  <Card key={collection?.cash_transaction_id}>
                    <CardContent className="p-4 space-y-4">
                      {/* Header */}
                      <div className="flex items-center justify-between">
                        <div>
                          <h3 className="font-semibold">{collection?.name}</h3>
                          <p className="text-sm text-muted-foreground">
                            {formatDateTime(
                              collection?.transaction_creation_date
                            )}
                          </p>
                        </div>
                        <p className="text-xl font-bold text-success">
                          ₹{collection.amount.toLocaleString()}
                        </p>
                      </div>

                      {/* Payment Info */}
                      <div className="flex justify-between gap-3">
                        <div
                          className={`p-1 rounded flex items-center gap-1 ${getMethodColor(
                            collection?.payment_mode
                          )}`}
                        >
                          <PaymentIcon
                            method={collection?.payment_mode?.toUpperCase()}
                          />
                          <span className="text-sm font-medium capitalize">
                            {collection?.payment_mode}
                          </span>
                        </div>
                        <div>
                          <Badge
                            variant="default"
                            className={`${
                              collection?.status === 'REJECTED'
                                ? 'bg-red-500 text-white'
                                : 'bg-success text-success-foreground'
                            }`}
                          >
                            {collection?.status}
                          </Badge>
                        </div>
                      </div>

                      {/* Bank Reference No */}
                      {collection?.bank_reference_no && (
                        <div className="w-full">
                          <Badge
                            variant="secondary"
                            className="w-full justify-center text-xs py-1"
                          >
                            Ref: {collection?.bank_reference_no}
                          </Badge>
                        </div>
                      )}

                      {collection?.payment_mode === 'ONLINE' &&
                        collection?.status === 'REQUESTED' &&
                        collection?.expiry_by && (
                          <div className="mt-1">
                            <CountdownTimer
                              expiryTimestamp={collection?.expiry_by}
                            />
                          </div>
                        )}

                      {/* Rejection Reason */}
                      {collection?.status === 'REJECTED' &&
                        collection?.rejection_reason && (
                          <div className="bg-red-50 border border-red-200 p-2 rounded-lg">
                            <p className="text-sm text-red-600">
                              <strong>Reason:</strong>{' '}
                              {collection?.rejection_reason}
                            </p>
                          </div>
                        )}

                      {/* Comment */}
                      {collection?.comments && (
                        <div className="bg-muted/30 border p-2 rounded-lg">
                          <p className="text-sm text-muted-foreground">
                            <strong>Comment:</strong> {collection?.comments}
                          </p>
                        </div>
                      )}

                      {/* Receipt Section */}
                      <div className="border-t pt-3">
                        {collection?.image_url ? (
                          <div className="flex items-center gap-2">
                            <ImageIcon className="h-4 w-4 text-muted-foreground" />
                            <a
                              href={collection.image_url}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="text-sm underline"
                            >
                              View Receipt
                            </a>
                          </div>
                        ) : (
                          // <Button size="sm" variant="outline" className="w-full">
                          //   <Upload className="h-4 w-4 mr-1" />
                          //   Upload Receipt
                          // </Button>
                          <></>
                        )}
                      </div>

                      {/* Actions */}
                      {collection?.payment_mode !== 'ONLINE' && (
                        <div className="flex gap-3 justify-end">
                          <Button
                            size="icon"
                            className="bg-blue-500 hover:bg-blue-600 text-white rounded-full"
                            onClick={() => onEdit(collection)}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            size="icon"
                            className="bg-red-500 hover:bg-red-600 text-white rounded-full"
                            onClick={() => {
                              setDeleteCollectionConfig({
                                show: true,
                                data: collection,
                              });
                            }}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>

          {/* Quick Actions */}
          {/* <div className="space-y-3">
        <Button
          variant="outline"
          size="mobile"
          className="w-full"
          onClick={() => navigate("/end-of-day")}
        >
          End of Day Report
        </Button>
      </div> */}
        </>
      )}

      {wareHouseList && (
        <WareHouseListPopupWithDropdown
          open={showWareHouseList}
          onClose={() => setShowWareHouseList(false)}
          wareHouseList={wareHouseList}
          onSubmit={onSelectOfWareHouse}
        />
      )}
      {showEditModal && toggleTxn && (
        <EditCollectionModal
          openToggle={showEditModal}
          onClose={() => setShowEditModal(false)}
          collection={toggleTxn}
          onUpdate={(data) => onUpdate(data)}
        />
      )}
      {showAddForm && orders && kycStatus &&(
        <AddCollectionDrawer
          openToggle={showAddForm}
          onClose={() => setShowAddForm(false)}
          outstanding_amount={orders?.outstanding_amount}
          onSubmit={(data) => {
            setNewCollection(data);
            triggerPaymentAsync(data);
          }}
          orderDetails={orders}
          kycStatus={kycStatus}
          cashCollection={cashCollected}
        />
      )}
      {deleteCollectionConfig?.show === true && (
        <ConfirmDialog
          open={deleteCollectionConfig.show}
          onOpenChange={() => setDeleteCollectionConfig({} as any)}
          onConfirm={handleCollectionDeleteConfirm}
        >
          <div className="text-center">
            Are you sure to delete this collection?
          </div>
        </ConfirmDialog>
      )}
      <QrDrawer
        open={showQr}
        onClose={() => setShowQr(false)}
        qrData={qrData}
      />
      {isUpdating ||
        isDeleting ||
        isFetchingOutStandingAmt ||
        (isFetchingInventory && (
          <Spinner label="Please wait while we are fetching details..." />
        ))}
    </Layout>
  );
};

export default Collections;
