import { useState } from 'react';
import { Button } from './button';
import { Card } from './card';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from './dialog';
import { Input } from './input';
import { toast } from './use-toast';
import { updateWholesaleOrderQntyAsync } from '@/configs/requests/order-service';
import { closeAgentTaskAsync } from '@/configs/requests/dashbord-service';
import { useTaskStore } from '@/hooks/useTasks';

export const DeliveryDialoge = ({
  selectedOrderForDeliver,
  deliverDialogOpen,
  setDeliverDialogOpen,
}) => {
  const [deliverQuantities, setDeliverQuantities] = useState({});
  const [verificationMode, setVerificationMode] = useState('NONE'); // "OTP" | "POD" | "BOTH" | "NONE"
  const [isDispatching, setIsDispatching] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);
  const [deliverConfirmationItems, setDeliverConfirmationItems] = useState([]);
  const [showDeliverConfirmation, setShowDeliverConfirmation] = useState(false);
  const selectedTask = useTaskStore((state) => state.selectedTask);
  const handleDeliver = () => {
    const items =
      selectedOrderForDeliver.freeFlowEntityItemDTOList
        ?.filter((item) => item.itemTypeId === 1)
        ?.map((item) => {
          const deliverQty = deliverQuantities[item.itemId] || 0;
          return {
            itemId: item.itemId,
            itemName: item.itemName,
            quantity: deliverQty,
          };
        }) || [];
    const totalDeliverQuantity = items.reduce(
      (sum, item) => sum + item.quantity,
      0
    );
    if (totalDeliverQuantity <= 0) {
      toast({
        title: 'Error',
        description: 'Total deliver quantity must be greater than 0',
        variant: 'destructive',
      });
      return;
    }
    setDeliverConfirmationItems(items.filter((item) => item.quantity > 0));
    setShowDeliverConfirmation(true);
    confirmDeliver();
  };

  const handleDeliverOtpOpen = () => {};

  const isDeliverEnabled = true;

  const confirmDeliver = async () => {
    setIsDispatching(true);
    try {
      // Only deliver items with itemTypeId === 1 and quantity > 0
      const items =
        selectedOrderForDeliver.freeFlowEntityItemDTOList
          ?.filter((item) => item.itemTypeId === 1)
          ?.map((item) => {
            const deliverQty = deliverQuantities[item.itemId] || 0;
            return {
              itemId: item.itemId,
              quantity: deliverQty,
              itemTypeId: item.itemTypeId,
            };
          })
          .filter((item) => item.quantity > 0) || [];
      // Use deliverWholesaleOrder API if available, else updateWholesaleOrderStatusWithQty with entity_status: 'DELIVERED'
      await updateWholesaleOrderQntyAsync({
        id: selectedOrderForDeliver.id,
        entity_status: 'DELIVERED',
        items: items,
      });
      toast({
        title: 'Success',
        description: `Order ${
          selectedOrderForDeliver.orderId || selectedOrderForDeliver.id
        } has been delivered successfully`,
      });
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
        setDeliverDialogOpen(false);
        setDeliverQuantities({});
      }
    } catch (error) {
      console.error('Deliver error:', error);
      toast({
        title: 'Error',
        description: error.message || 'Failed to deliver order',
        variant: 'destructive',
      });
    } finally {
      setIsDispatching(false);
    }
  };

  return (
    <Dialog open={deliverDialogOpen} onOpenChange={setDeliverDialogOpen}>
      <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto p-4 sm:p-6">
        <DialogHeader>
          <DialogTitle className="text-lg sm:text-xl">
            Deliver Order
          </DialogTitle>
        </DialogHeader>
        {selectedOrderForDeliver && (
          <div className="space-y-4">
            <div className="bg-gray-50 p-4 rounded-lg">
              <h4 className="font-medium mb-2">Order Details</h4>
              <p>
                <strong>Order ID:</strong>{' '}
                {selectedOrderForDeliver.orderId || selectedOrderForDeliver.id}
              </p>
              <p>
                <strong>Status:</strong> {selectedOrderForDeliver.entityStatus}
              </p>
            </div>
            <div className="space-y-3">
              <h4 className="font-medium">Items to Deliver</h4>

              {/* Desktop Table View */}
              <div className="hidden lg:block border rounded-lg overflow-hidden">
                <table className="w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">
                        Item Name
                      </th>
                      <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">
                        Original Quantity
                      </th>
                      <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">
                        Selling Price
                      </th>
                      <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">
                        Total
                      </th>
                      <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">
                        Dispatched Quantity
                      </th>
                      <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">
                        Deliver Quantity
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {selectedOrderForDeliver.freeFlowEntityItemDTOList
                      ?.filter((item) => item.itemTypeId === 1)
                      ?.map((item) => {
                        // Get original quantity (from additionalInfo or item.quantity)
                        let originalQty = item.quantity || 0;
                        if (item.additionalInfo) {
                          try {
                            const parsed = JSON.parse(item.additionalInfo);
                            const orderedQuantity = parsed.find(
                              (info) => info.infoType === 'ordered_quantity'
                            );
                            if (orderedQuantity) {
                              originalQty = parseInt(
                                orderedQuantity.value || '0',
                                10
                              );
                            }
                          } catch (e) {
                            // ignore
                          }
                        }
                        const dispatchedQty = item.dispatchedQuantity || 0;
                        const deliverQty = deliverQuantities[item.itemId] || 0;
                        const sellingPrice = item.unitSellPrice || 0;
                        const total = dispatchedQty * sellingPrice;
                        return (
                          <tr key={item.itemId} className="hover:bg-gray-50">
                            <td className="px-4 py-3 text-sm">
                              <div>
                                <p className="font-medium">{item.itemName}</p>
                                <p className="text-xs text-gray-500">
                                  {item.itemUomName}
                                </p>
                              </div>
                            </td>
                            <td className="px-4 py-3 text-sm text-right">
                              {originalQty.toLocaleString()}
                            </td>
                            <td className="px-4 py-3 text-sm text-right">
                              ₹
                              {sellingPrice.toLocaleString('en-IN', {
                                minimumFractionDigits: 2,
                              })}
                            </td>
                            <td className="px-4 py-3 text-sm text-right font-medium">
                              ₹
                              {total.toLocaleString('en-IN', {
                                minimumFractionDigits: 2,
                              })}
                            </td>
                            <td className="px-4 py-3 text-sm text-right font-medium">
                              {dispatchedQty.toLocaleString()}
                            </td>
                            <td className="px-4 py-3 text-sm text-right">
                              <div className="flex justify-end">
                                <Input
                                  type="text"
                                  min="0"
                                  max={dispatchedQty}
                                  value={deliverQty}
                                  onChange={(e) => {
                                    const newQty = Math.min(
                                      parseInt(e.target.value) || 0,
                                      dispatchedQty
                                    );
                                    setDeliverQuantities((prev) => ({
                                      ...prev,
                                      [item.itemId]: newQty,
                                    }));
                                  }}
                                  className="w-24 text-right"
                                />
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                  </tbody>
                </table>
              </div>

              {/* Mobile Card View */}
              <div className="lg:hidden space-y-4">
                {selectedOrderForDeliver.freeFlowEntityItemDTOList
                  ?.filter((item) => item.itemTypeId === 1)
                  ?.map((item) => {
                    // Get original quantity (from additionalInfo or item.quantity)
                    let originalQty = item.quantity || 0;
                    if (item.additionalInfo) {
                      try {
                        const parsed = JSON.parse(item.additionalInfo);
                        const orderedQuantity = parsed.find(
                          (info) => info.infoType === 'ordered_quantity'
                        );
                        if (orderedQuantity) {
                          originalQty = parseInt(
                            orderedQuantity.value || '0',
                            10
                          );
                        }
                      } catch (e) {
                        // ignore
                      }
                    }
                    const dispatchedQty = item.dispatchedQuantity || 0;
                    const deliverQty = deliverQuantities[item.itemId] || 0;
                    const sellingPrice = item.unitSellPrice || 0;
                    const total = dispatchedQty * sellingPrice;

                    return (
                      <Card key={item.itemId} className="p-4">
                        <div className="space-y-3">
                          <div>
                            <span className="text-sm font-medium text-muted-foreground">
                              Item Name:
                            </span>
                            <p className="font-medium">{item.itemName}</p>
                            <p className="text-xs text-gray-500">
                              {item.itemUomName}
                            </p>
                          </div>
                          <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                              <span className="text-muted-foreground">
                                Original Qty:
                              </span>
                              <p>{originalQty.toLocaleString()}</p>
                            </div>
                            <div>
                              <span className="text-muted-foreground">
                                Selling Price:
                              </span>
                              <p>
                                ₹
                                {sellingPrice.toLocaleString('en-IN', {
                                  minimumFractionDigits: 2,
                                })}
                              </p>
                            </div>
                            <div>
                              <span className="text-muted-foreground">
                                Total:
                              </span>
                              <p className="font-medium">
                                ₹
                                {total.toLocaleString('en-IN', {
                                  minimumFractionDigits: 2,
                                })}
                              </p>
                            </div>
                            <div>
                              <span className="text-muted-foreground">
                                Dispatched Qty:
                              </span>
                              <p>{dispatchedQty.toLocaleString()}</p>
                            </div>
                            <div>
                              <span className="text-muted-foreground">
                                Deliver Qty:
                              </span>
                              <Input
                                type="number"
                                min="0"
                                max={dispatchedQty}
                                value={deliverQty}
                                onChange={(e) => {
                                  const newQty = Math.min(
                                    parseInt(e.target.value) || 0,
                                    dispatchedQty
                                  );
                                  setDeliverQuantities((prev) => ({
                                    ...prev,
                                    [item.itemId]: newQty,
                                  }));
                                }}
                                className="mt-1"
                              />
                            </div>
                          </div>
                        </div>
                      </Card>
                    );
                  })}
              </div>
            </div>
          </div>
        )}
        <DialogFooter className="flex flex-col sm:flex-row justify-end gap-2">
          <Button
            variant="outline"
            onClick={() => {
              setDeliverDialogOpen(false);
              setDeliverQuantities({});
            }}
            disabled={isDispatching}
            className="w-full sm:w-auto"
            size="sm"
          >
            Cancel
          </Button>
          {/* Conditionally render OTP and POD buttons based on verificationMode */}
          {['OTP', 'BOTH'].includes(verificationMode) && !otpVerified && (
            <Button
              variant="secondary"
              onClick={handleDeliverOtpOpen}
              disabled={isDispatching}
              className="w-full sm:w-auto"
              size="sm"
            >
              Verify OTP
            </Button>
          )}
          {/* {['POD', 'BOTH'].includes(verificationMode) &&
            !podVerified &&
            isPrivilegeAllowed(privileges.D2C_FILE_UPLOAD) && (
              <Button
                variant="secondary"
                onClick={() => setPodDialogOpen(true)}
                disabled={isDispatching}
                className="w-full sm:w-auto"
                size="sm"
              >
                Upload POD
              </Button>
            )} */}
          <Button
            onClick={handleDeliver}
            disabled={isDispatching || !isDeliverEnabled}
            className="bg-primary hover:bg-primary-hover w-full sm:w-auto"
            size="sm"
          >
            {isDispatching ? 'Delivering...' : 'Deliver Order'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
