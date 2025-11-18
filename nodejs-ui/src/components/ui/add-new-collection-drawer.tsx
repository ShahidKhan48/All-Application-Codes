import { useState } from 'react';
import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
} from '@/components/ui/drawer';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { CheckCircle } from 'lucide-react';
import { uplodeFileRequest } from '@/configs/requests/collection-service';
import { Collection } from '@/data/mockData';
import PaymentIcon from '../molecules/payment-icon';
import { IFetchStoreDTCAmt } from '@/lib/common-type';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './select';
import { useTaskStore } from '@/hooks/useTasks';

const paymentMethods = [
  { value: 'CASH', label: 'Cash' },
  { value: 'NEFT', label: 'NEFT' },
  { value: 'CHEQUE', label: 'Cheque' },
  { value: 'PAYMENT_LINK', label: 'Payment Link' },
  { value: 'QR', label: 'QR' },
];

export default function AddCollectionDrawer({
  openToggle,
  onClose,
  onSubmit,
  outstanding_amount,
  orderDetails,
  kycStatus,
  cashCollection
}: {
  openToggle: boolean;
  onClose: () => void;
  onSubmit: (data: Partial<Collection>) => void;
  outstanding_amount: string;
  orderDetails: IFetchStoreDTCAmt;
  kycStatus: boolean
  cashCollection: number
}) {
  const selectedStore = useTaskStore((state) => state?.slectedStoreInfo);
  const [newCollection, setNewCollection] = useState<Partial<Collection>>({
    amount: 0,
    payment_mode: '',
    bank_reference_no: '',
    comments: '',
    image_url: '',
    mobile_no: selectedStore?.contact_number,
    cheque_date: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [viewDetails, setViewDetails] = useState(false);

  const handleChange = (field: keyof Collection, value: any) => {
    setNewCollection((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' })); // clear error on change
  };

  const handleFileUpload = async (
    e: React.ChangeEvent<HTMLInputElement>,
    field: keyof Collection
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const result = await uplodeFileRequest(file);
      handleChange(field, result?.[0]);
    } catch (err) {
      console.error('File upload failed:', err);
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    const outstanding = Number(outstanding_amount);

    // Amount validation
    if (!newCollection.amount || newCollection.amount <= 0) {
      newErrors.amount = 'Amount is required';
    } else if (newCollection.amount > outstanding) {
      newErrors.amount = `Amount cannot exceed outstanding amount (₹${outstanding})`;
    }
    else if (kycStatus && ((newCollection.amount + cashCollection) > 200000 )) {
      newErrors.amount = `Amount cannot exceed outstanding amount ₹200000`;
    }

    else if (!kycStatus && ((newCollection.amount + cashCollection) > 50000 )) {
      newErrors.amount = `Amount cannot exceed outstanding amount ₹50,000`;
    }
     else if (!kycStatus && newCollection.amount > 50000 ) {
      newErrors.amount = `Amount cannot exceed outstanding amount ₹5,000`;
    }

    // Payment method validation
    if (!newCollection.payment_mode) {
      newErrors.payment_mode = 'Payment method is required';
    }

    // NEFT validation
    if (newCollection.payment_mode === 'NEFT') {
      if (!newCollection.bank_reference_no) {
        newErrors.bank_reference_no = 'Transaction ID is required';
      }
      if (!newCollection.image_url) {
        newErrors.image_url = 'Receipt upload is required';
      }
    }

    // CHEQUE validation
    if (newCollection.payment_mode === 'CHEQUE') {
      if (!newCollection.bank_reference_no) {
        newErrors.bank_reference_no = 'Cheque number is required';
      }
      if (!newCollection.cheque_date) {
        newErrors.cheque_date = 'Cheque date is required';
      }
      if (!newCollection.image_url) {
        newErrors.image_url = 'Cheque image upload is required';
      }
    }

    // PAYMENT LINK validation
    if (newCollection.payment_mode === 'PAYMENT_LINK') {
      if (!newCollection.mobile_no) {
        newErrors.mobile_no = 'Mobile number is required';
      } else if (!/^\d{10}$/.test(newCollection.mobile_no)) {
        newErrors.mobile_no = 'Enter a valid 10-digit mobile number';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (!validateForm()) return;
    onSubmit(newCollection);
    onClose();
  };

  return (
    <Drawer open={openToggle} onOpenChange={onClose}>
      <DrawerContent className="sm:max-w-lg p-6 flex flex-col h-[90vh]">
        <DrawerHeader className="flex items-center justify-between">
          <div className="flex flex-col">
            <DrawerTitle className="text-lg font-semibold">
              Add New Collection
            </DrawerTitle>
            <DrawerDescription className="text-sm text-muted-foreground">
              Record a new payment collection
            </DrawerDescription>
            <DrawerDescription className="text-sm font-medium text-primary mt-1">
              Outstanding Amount:{' '}
              <span className="font-semibold">₹{outstanding_amount}</span>
            </DrawerDescription>
          </div>

          <Button
            variant="outline"
            size="sm"
            className="ml-4"
            onClick={() => setViewDetails(!viewDetails)}
          >
            {viewDetails ? 'Hide Orders' : 'View Orders'}
          </Button>
        </DrawerHeader>

        <div className="flex-1 overflow-y-auto px-1 pb-6">
          {/* Orders/RTV Tabs */}
          {viewDetails && (
            <Tabs defaultValue="orders" className="w-full mt-4">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="orders">Orders</TabsTrigger>
                <TabsTrigger value="rtv_orders">RTV Orders</TabsTrigger>
              </TabsList>

              <TabsContent value="orders">
                {orderDetails.orders?.map((order) => (
                  <div
                    key={order?.payload?.id}
                    className="p-3 border rounded-lg mb-3"
                  >
                    <div className="grid grid-cols-3 gap-4 text-sm">
                      {/* Row 1 */}
                      <div className="flex flex-col">
                        <span className="text-xs text-gray-500">Order ID</span>
                        <span className="font-semibold text-black">
                          {order?.payload?.id}
                        </span>
                      </div>
                      <div className="flex flex-col">
                        <span className="text-xs text-gray-500">
                          Order Date
                        </span>
                        <span className="font-semibold text-black">
                          {order?.payload?.createdAt}
                        </span>
                      </div>
                      <div className="flex flex-col">
                        <span className="text-xs text-gray-500">
                          Delivery Date
                        </span>
                        <span className="font-semibold text-black">
                          {order?.payload?.entityDate}
                        </span>
                      </div>

                      {/* Row 2 */}
                      <div className="flex flex-col">
                        <span className="text-xs text-gray-500">
                          Total Order Value
                        </span>
                        <span className="font-semibold text-green-600">
                          ₹{order?.payload?.totalAmount}
                        </span>
                      </div>
                      <div className="flex flex-col">
                        <span className="text-xs text-gray-500">
                          Outstanding Amount
                        </span>
                        <span className="font-semibold text-red-600">
                          ₹{order?.payload?.outstandingAmount}
                        </span>
                      </div>
                      <div className="flex flex-col">
                        <span className="text-xs text-gray-500">
                          In Transit Amount
                        </span>
                        <span className="font-semibold text-blue-600">
                          ₹{order?.amount_pending_for_approval}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </TabsContent>

              <TabsContent value="rtv_orders">
                {orderDetails.rtv_orders?.length === 0 ? (
                  <p className="text-muted-foreground text-sm">
                    No RTV Orders available
                  </p>
                ) : (
                  orderDetails.rtv_orders.map((rtv, idx) => (
                    <div
                      key={idx}
                      className="p-3 border rounded-lg mb-3 space-y-1"
                    >
                      <p className="text-sm font-medium">RTV Order</p>
                    </div>
                  ))
                )}
              </TabsContent>
            </Tabs>
          )}

          <div className="space-y-4 py-2">
            {/* Amount */}
            <div>
              <Label htmlFor="amount">Amount *</Label>
              <Input
                id="amount"
                type="number"
                placeholder="Enter amount"
                value={newCollection.amount ?? ''}
                onChange={(e) => handleChange('amount', Number(e.target.value))}
              />
              {errors.amount && (
                <p className="text-destructive text-xs mt-1">{errors.amount}</p>
              )}
            </div>

            {/* CASH */}
            {/* {newCollection.payment_mode === 'CASH' && (
              <div>
                <Label>Upload Receipt</Label>
                <Input
                  onChange={(e) => handleFileUpload(e, 'image_url')}
                  type="file"
                  accept="image/*"
                />
              </div>
            )} */}

            {/* NEFT */}
            {newCollection.payment_mode === 'NEFT' && (
              <>
                <div>
                  <Label htmlFor="txn">Transaction ID *</Label>
                  <Input
                    id="txn"
                    placeholder="Enter transaction ID"
                    value={newCollection.bank_reference_no ?? ''}
                    onChange={(e) =>
                      handleChange('bank_reference_no', e.target.value)
                    }
                  />
                  {errors.bank_reference_no && (
                    <p className="text-destructive text-xs mt-1">
                      {errors.bank_reference_no}
                    </p>
                  )}
                </div>
                <div>
                  <Label>Upload Receipt *</Label>
                  <Input
                    onChange={(e) => handleFileUpload(e, 'image_url')}
                    type="file"
                    accept="image/*"
                  />
                  {errors.image_url && (
                    <p className="text-destructive text-xs mt-1">
                      {errors.image_url}
                    </p>
                  )}
                </div>
              </>
            )}

            {/* CHEQUE */}
            {newCollection.payment_mode === 'CHEQUE' && (
              <>
                <div>
                  <Label htmlFor="chequeNo">Cheque Number *</Label>
                  <Input
                    id="chequeNo"
                    placeholder="Enter cheque number"
                    value={newCollection.bank_reference_no ?? ''}
                    onChange={(e) =>
                      handleChange('bank_reference_no', e.target.value)
                    }
                  />
                  {errors.bank_reference_no && (
                    <p className="text-destructive text-xs mt-1">
                      {errors.bank_reference_no}
                    </p>
                  )}
                </div>
                <div>
                  <Label htmlFor="chequeDate">Cheque Date *</Label>
                  <Input
                    id="chequeDate"
                    type="date"
                    value={newCollection.cheque_date ?? ''}
                    onChange={(e) =>
                      handleChange('cheque_date', e.target.value)
                    }
                  />
                  {errors.cheque_date && (
                    <p className="text-destructive text-xs mt-1">
                      {errors.cheque_date}
                    </p>
                  )}
                </div>
                <div>
                  <Label>Upload Cheque *</Label>
                  <Input
                    onChange={(e) => handleFileUpload(e, 'image_url')}
                    type="file"
                    accept="image/*"
                  />
                  {errors.image_url && (
                    <p className="text-destructive text-xs mt-1">
                      {errors.image_url}
                    </p>
                  )}
                </div>
              </>
            )}

            {/* PAYMENT LINK */}
            {newCollection.payment_mode === 'PAYMENT_LINK' && (
              <div>
                <Label htmlFor="mobile">Mobile Number *</Label>
                <Input
                  id="mobile"
                  type="tel"
                  placeholder="Enter mobile number"
                  value={newCollection.mobile_no ?? ''}
                  onChange={(e) => handleChange('mobile_no', e.target.value)}
                />
                {errors.mobile_no && (
                  <p className="text-destructive text-xs mt-1">
                    {errors.mobile_no}
                  </p>
                )}
              </div>
            )}

            {/* QR */}
            {newCollection.payment_mode === 'QR' && (
              <p className="text-sm text-muted-foreground">
                QR Payment requires only amount.
              </p>
            )}

            {/* Payment Method */}
            <div>
              <Label>Payment Method *</Label>
              <Select
                value={newCollection.payment_mode || ''}
                onValueChange={(value) =>
                  handleChange(
                    'payment_mode',
                    value as Collection['payment_mode']
                  )
                }
              >
                <SelectTrigger className="mt-2">
                  <SelectValue placeholder="Select a payment method" />
                </SelectTrigger>
                <SelectContent>
                  {paymentMethods.map((method) => (
                    <SelectItem key={method.value} value={method.value}>
                      <div className="flex items-center gap-2">
                        <PaymentIcon method={method.value} />
                        <span>{method.label}</span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              {errors.payment_mode && (
                <p className="text-destructive text-xs mt-1">
                  {errors.payment_mode}
                </p>
              )}
            </div>
            {newCollection?.payment_mode === 'CASH' && (
              <span className='text-xs text-green-500 py-[5px]'>
                {
                  '*As per compliance rules, a maximum of ₹2,00,000 can be collected in cash from a vendor in a single day.For larger payments, please use UPI, NEFT"'
                }
              </span>
            )}

            {newCollection.payment_mode !== 'QR' &&
              newCollection.payment_mode !== 'PAYMENT_LINK' && (
                <div>
                  <Label htmlFor="comments">Comments</Label>
                  <Input
                    id="comments"
                    type="text"
                    placeholder="Enter comments"
                    value={newCollection.comments ?? ''}
                    onChange={(e) => handleChange('comments', e.target.value)}
                  />
                  {/* {errors.mobile_no && (
                    <p className="text-destructive text-xs mt-1">
                      {errors.mobile_no}
                    </p>
                  )} */}
                </div>
              )}

            {/* Buttons */}
            <div className="flex gap-3 pt-4">
              <Button
                variant="success"
                className="flex-1"
                onClick={handleSubmit}
              >
                <CheckCircle className="w-4 h-4 mr-2" />
                Add Collection
              </Button>
              <Button variant="outline" className="flex-1" onClick={onClose}>
                Cancel
              </Button>
            </div>
          </div>
        </div>
      </DrawerContent>
    </Drawer>
  );
}
