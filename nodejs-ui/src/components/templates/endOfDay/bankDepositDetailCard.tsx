import { useEffect, useMemo, useState } from "react";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { Building, Camera, Plus, Trash2 } from "lucide-react";
import { uplodeFileRequest } from "@/configs/requests/collection-service";
import { useMutation } from "@tanstack/react-query";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useTaskStore } from "@/hooks/useTasks";
import { rejectCashTxnAsync } from "@/configs/requests/start-end.service";

export const PaymentMethods = [
  { id: 2, value: "PayNearby", text: "Pay Near By" },
  { id: 3, value: "Others", text: "Others" },
];

export default function BankDepositCard({ onChangeDeposits }) {
  const overallCollectionData = useTaskStore((m) => m.overallCollectionData);
  const [deposits, setDeposits] = useState([]);
  const [errors, setErrors] = useState({}); // track validation errors
  const { mutateAsync, isPending } = useMutation({
    mutationKey: ["uplodeFileRequest"],
    mutationFn: uplodeFileRequest,
  });
  const {
    mutateAsync: rejectCashTxnMutateAsync,
    isPending: rejectCashTxnPending,
  } = useMutation({
    mutationKey: ["rejectCashTxnAsync"],
    mutationFn: rejectCashTxnAsync,
  });

  const [openIndex, setOpenIndex] = useState(null); // track which dialog is open
  const totalAddedDepositAmount = useMemo(
    () => deposits?.map((m) => Number(m.amount)).reduce((a, b) => a + b, 0),
    [deposits]
  );
  const cashAmountToBeDeposited = Number(
    overallCollectionData?.["agent-cash-collected-amount"]
      ?.collection_grouped_values?.cash
  );
  const closeAdd = totalAddedDepositAmount >= cashAmountToBeDeposited;

  const addDeposit = () => {
    const newIndex = deposits.length;
    const data = [
      ...deposits,
      { type: "", amount: "", slipNumber: "", file: null },
    ];
    setDeposits(data);
    onChangeDeposits(data);
    setOpenIndex(newIndex); // open dialog immediately
  };

  const updateDeposit = (index, field, value) => {
    const updated = [...deposits];
    updated[index][field] = value;
    setDeposits(updated);
    onChangeDeposits(updated);
  };

  const handleFileUpload = async (index, e) => {
    const file = e.target.files[0];
    if (!file) return;
    try {
      const result = await mutateAsync(file);
      updateDeposit(index, "file", result?.[0]);
    } catch (err) {
      console.error("File upload failed:", err);
    }
  };

  const deleteDeposit = (index, cash_txn_id) => {
    if (cash_txn_id) {
      // delete cash transaction from db
      rejectCashTxnMutateAsync({
        input: { cash_txn_id },
      });
    }
    const updated = deposits.filter((_, i) => i !== index);
    setDeposits(updated);
    onChangeDeposits(updated);
  };

  const validateDeposit = (deposit, index) => {
    const newErrors = {} as any;
    if (!deposit.amount) newErrors.amount = "Amount is required";
    if (!deposit.slipNumber) newErrors.slipNumber = "Slip number is required";
    if (!deposit.type) newErrors.type = "Select Deposit type";
    if (!deposit.file) newErrors.file = "Upload any deposit slip";

    setErrors((prev) => ({ ...prev, [index]: newErrors }));
    return Object.keys(newErrors).length === 0;
  };

  useEffect(() => {
    const depositSlipData =
      overallCollectionData?.["agent-cash-collected-amount"]
        ?.deposit_slip_details;

    // prefill deposit slips if its already submitted earlier
    if (depositSlipData?.length > 0) {
      const updated = depositSlipData?.map((m) => ({
        ...m,
        type: m.name,
        amount: m.amount,
        slipNumber: m.bank_reference_no,
        file: m.image_url,
      }));
      setDeposits(updated);
      onChangeDeposits(updated);
    }
  }, [overallCollectionData]);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Building className="w-5 h-5" />
          <h1 className="text-lg sm:text-xl lg:text-2xl font-semibold text-foreground">
            Bank Deposit Details
          </h1>
        </CardTitle>
        <CardDescription>
          Cash amount to be deposited: ₹
          {cashAmountToBeDeposited?.toLocaleString()}
        </CardDescription>

        <Button
          onClick={addDeposit}
          variant="outline"
          size="sm"
          className="mt-2"
          disabled={closeAdd}
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Deposit
        </Button>
      </CardHeader>

      <CardContent className="space-y-4">
        {deposits.length === 0 ? (
          <p className="text-muted-foreground text-sm">
            No deposits added yet.
          </p>
        ) : (
          deposits.map((deposit, index) => (
            <div
              key={index}
              className="flex flex-col gap-1 border p-3 rounded-lg"
            >
              <span className="text-sm text-left flex gap-2">
                <span className="font-[600]">Slip# </span>
                <span className="line-clamp-1">{deposit.slipNumber}</span>
              </span>
              <span className="text-xs text-muted-foreground flex gap-1">
                <span>₹{deposit.amount || "0"}</span>({deposit.type})
              </span>
              <div className="flex items-center justify-between">
                <img className="w-[30px] h-[30px]" src={deposit.file} />
                <div className="flex gap-2">
                  {/* Edit in Dialog */}
                  <Dialog
                    open={openIndex === index}
                    onOpenChange={(isOpen) =>
                      setOpenIndex(isOpen ? index : null)
                    }
                  >
                    <DialogTrigger asChild>
                      <Button size="sm" variant="outline">
                        Edit
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-md">
                      <DialogHeader>
                        <DialogTitle>Edit Deposit</DialogTitle>
                      </DialogHeader>

                      <div className="space-y-4">
                        <div>
                          <Label htmlFor={`depositType-${index}`}>
                            Deposit Type *
                          </Label>
                          <Select
                            value={deposit.type || ""}
                            onValueChange={(val) =>
                              updateDeposit(index, "type", val)
                            }
                          >
                            <SelectTrigger id={`depositType-${index}`}>
                              <SelectValue placeholder="Select deposit type" />
                            </SelectTrigger>
                            <SelectContent>
                              {PaymentMethods.map((method) => (
                                <SelectItem value={method.value}>
                                  {method.text}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>

                          {errors[index]?.type && (
                            <p className="text-red-500 text-sm">
                              {errors[index].type}
                            </p>
                          )}
                        </div>

                        <div>
                          <Label htmlFor={`depositAmount-${index}`}>
                            Deposit Amount *
                          </Label>
                          <Input
                            id={`depositAmount-${index}`}
                            type="number"
                            placeholder="Enter deposited amount"
                            value={deposit.amount}
                            onChange={(e) =>
                              updateDeposit(index, "amount", e.target.value)
                            }
                          />
                          {errors[index]?.amount && (
                            <p className="text-red-500 text-sm">
                              {errors[index].amount}
                            </p>
                          )}
                        </div>

                        <div>
                          <Label htmlFor={`depositSlip-${index}`}>
                            Deposit Slip Number *
                          </Label>
                          <Input
                            id={`depositSlip-${index}`}
                            placeholder="Enter deposit slip number"
                            value={deposit.slipNumber}
                            onChange={(e) =>
                              updateDeposit(index, "slipNumber", e.target.value)
                            }
                          />
                          {errors[index]?.slipNumber && (
                            <p className="text-red-500 text-sm">
                              {errors[index].slipNumber}
                            </p>
                          )}
                        </div>

                        <div>
                          <Label>Upload Deposit Slip Photo</Label>
                          <Button
                            variant="outline"
                            className="w-full mt-2"
                            size="sm"
                            onClick={() =>
                              document
                                .getElementById(`fileDepositSlip-${index}`)
                                .click()
                            }
                          >
                            <Camera className="w-4 h-4 mr-2" />
                            Take Photo of Deposit Slip
                          </Button>
                          {errors[index]?.file && (
                            <p className="text-red-500 text-sm">
                              {errors[index].file}
                            </p>
                          )}
                          <Input
                            id={`fileDepositSlip-${index}`}
                            onChange={(e) => handleFileUpload(index, e)}
                            type="file"
                            accept="image/*"
                            className="hidden"
                          />
                          <div className="mt-2">
                            {deposit?.file && (
                              <img
                                src={deposit.file}
                                decoding="async"
                                loading="lazy"
                                className="w-[50px] h-[50px]"
                              />
                            )}
                          </div>
                        </div>
                      </div>

                      <DialogFooter className="mt-4 flex flex-col gap-4">
                        {/* Cancel */}
                        <DialogClose asChild>
                          <Button variant="outline">Cancel</Button>
                        </DialogClose>

                        {/* OK button with validation */}
                        <DialogClose asChild>
                          <Button
                            disabled={isPending}
                            onClick={(e) => {
                              if (!validateDeposit(deposit, index)) {
                                e.preventDefault(); // prevent closing
                              }
                            }}
                          >
                            OK
                          </Button>
                        </DialogClose>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>

                  {/* Delete Button */}
                  <Button
                    size="sm"
                    variant="destructive"
                    disabled={rejectCashTxnPending}
                    onClick={() => {
                      deleteDeposit(index, deposit.cash_transaction_id);
                    }}
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  );
}
