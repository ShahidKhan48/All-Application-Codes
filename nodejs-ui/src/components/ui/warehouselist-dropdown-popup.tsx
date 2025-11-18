import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Relation } from '@/lib/common-type';

export default function WareHouseListPopupWithDropdown({
  open,
  onClose,
  wareHouseList,
  onSubmit,
}: {
  open: boolean;
  onClose: () => void;
  wareHouseList: Relation[];
  onSubmit: (data: Relation) => void;
}) {
  const [selected, setSelected] = useState<Relation | null>(
    null
  );

  const handleSubmit = () => {
    if (!selected) return;
    const parsed = selected as Relation;
    onSubmit(parsed);
    onClose(); // close popup after submit
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="w-[91%]">
        <DialogHeader>
          <DialogTitle>Select an Option</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <Select
            onValueChange={(value) => {
              const warehouse = wareHouseList.find(
                (item) => item?.userRealmId === value
              );
              setSelected(warehouse || null);
            }}
          >
            {' '}
            <SelectTrigger>
              <SelectValue placeholder="Choose an option" />
            </SelectTrigger>
            <SelectContent>
              {wareHouseList?.map((item) => (
                <SelectItem key={item?.userRealmId} value={item?.userRealmId}>
                  {item?.name || item?.businessName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <p className="text-sm text-gray-600">
            Selected:{' '}
            <span className="font-medium">
              {selected?.name ||  selected?.businessName || 'None'}
            </span>
          </p>

          <Button onClick={handleSubmit} disabled={!selected}>
            Submit
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
