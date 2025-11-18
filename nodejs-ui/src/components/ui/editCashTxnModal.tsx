import { useEffect, useState } from "react";
import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
} from "@/components/ui/drawer";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { CheckCircle } from "lucide-react";
import { Collection } from "@/data/mockData";
import { uplodeFileRequest } from "@/configs/requests/collection-service";
import PaymentIcon from "../molecules/payment-icon";

const paymentMethods = [
  { value: "CASH", label: "Cash" },
  { value: "NEFT", label: "NEFT" },
];

export default function EditCollectionDrawer({
  openToggle,
  onClose,
  collection,
  onUpdate,
}: {
  openToggle: boolean;
  onClose: () => void;
  collection: Collection;
  onUpdate: (data: Collection) => void;
}) {
  const [form, setForm] = useState<Collection>(collection);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setForm(collection);
    setErrors({});
  }, [collection]);

  const handleChange = (field: keyof Collection, value: any) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: "" })); // clear error on change
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
      console.error("File upload failed:", err);
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!form.amount || form.amount <= 0) newErrors.amount = "Amount is required";
    if (!form.payment_mode) newErrors.payment_mode = "Payment method is required";
    if (
      (form.payment_mode === "NEFT" || form.payment_mode === "CHEQUE") &&
      !form.bank_reference_no
    )
      newErrors.bank_reference_no = "Reference number is required";
    if (!form.image_url) newErrors.image_url = "Proof image is required";
    if (!form.comments || form.comments.trim() === "")
      newErrors.comments = "Comments are required";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleUpdate = () => {
    if (!validate()) return;
    onUpdate(form);
    onClose();
  };

  return (
    <Drawer open={openToggle} onOpenChange={onClose}>
      <DrawerContent className="p-4 sm:max-w-lg mx-auto max-h-[90%]">
        <DrawerHeader>
          <DrawerTitle>Edit Collection</DrawerTitle>
          <DrawerDescription>Update payment collection details.</DrawerDescription>
        </DrawerHeader>

        <div className="space-y-4 py-2 overflow-auto">
          {/* Amount */}
          <div>
            <Label htmlFor="amount">Amount *</Label>
            <Input
              id="amount"
              type="number"
              placeholder="Enter amount"
              value={form.amount}
              onChange={(e) => {
                const value = Number(e.target.value);
                if (value <= collection?.amount) handleChange("amount", value);
              }}
            />
            {errors.amount && <p className="text-red-500 text-sm">{errors.amount}</p>}
          </div>

          {/* Payment Method */}
          <div>
            <Label>Payment Method *</Label>
            <div className="grid grid-cols-2 gap-2 mt-2">
              {paymentMethods.map((method) => (
                <Button
                  key={method.value}
                  variant={form.payment_mode === method.value ? "default" : "outline"}
                  size="sm"
                  className="justify-start"
                  onClick={() => handleChange("payment_mode", method.value)}
                >
                  <PaymentIcon method={method.value} />
                  {method.label}
                </Button>
              ))}
            </div>
            {errors.payment_mode && (
              <p className="text-red-500 text-sm">{errors.payment_mode}</p>
            )}
          </div>

          {/* Reference Number */}
          {(form.payment_mode === "NEFT" || form.payment_mode === "CHEQUE") && (
            <div>
              <Label htmlFor="reference">Reference Number *</Label>
              <Input
                id="reference"
                placeholder={`Enter ${form.payment_mode} reference`}
                value={form.bank_reference_no || ""}
                onChange={(e) => handleChange("bank_reference_no", e.target.value)}
              />
              {errors.bank_reference_no && (
                <p className="text-red-500 text-sm">{errors.bank_reference_no}</p>
              )}
            </div>
          )}

          {/* Uploaded Image */}
          <div>
            <Label>Uploaded Proof *</Label>
            <div className="mt-2 flex items-center gap-3">
              {form.image_url && (
                <img
                  src={form?.image_url}
                  alt="Uploaded Proof"
                  className="w-32 h-32 object-cover rounded-md border"
                />
              )}
              <Button type="button" variant="outline" className="relative cursor-pointer">
                Upload
                <Input
                  type="file"
                  accept="image/*"
                  className="absolute inset-0 opacity-0 cursor-pointer"
                  onChange={(e) => handleFileUpload(e, "image_url")}
                />
              </Button>
            </div>
            {errors.image_url && <p className="text-red-500 text-sm">{errors.image_url}</p>}
          </div>

          {/* Comments */}
          <div>
            <Label htmlFor="comments">Comments *</Label>
            <Textarea
              id="comments"
              placeholder="Enter any remarks..."
              value={form.comments || ""}
              onChange={(e) => handleChange("comments", e.target.value)}
            />
            {errors.comments && <p className="text-red-500 text-sm">{errors.comments}</p>}
          </div>
        </div>
        {/* Buttons */}
        <div className="flex gap-3 pt-4">
          <Button variant="success" className="flex-1" onClick={handleUpdate}>
            <CheckCircle className="w-4 h-4 mr-2" />
            Update
          </Button>
          <Button variant="outline" className="flex-1" onClick={onClose}>
            Cancel
          </Button>
        </div>
      </DrawerContent>
    </Drawer>
  );
}
