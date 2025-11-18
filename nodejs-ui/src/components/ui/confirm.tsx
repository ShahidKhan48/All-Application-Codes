import { Button } from "./button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "./dialog";

const ConfirmDialog = ({
  open,
  onOpenChange,
  children,
  title,
  onConfirm,
}: {
  open: boolean;
  onOpenChange?: () => void;
  children: any;
  title?: any;
  onConfirm?: (action: "yes" | "no") => void;
}) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md rounded-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-center gap-2">
            <span className="text-lg font-semibold">{title || "Confirm"}</span>
          </DialogTitle>
        </DialogHeader>
        <div className="space-y-4">{children}</div>
        <DialogFooter>
          <div className="flex items-center justify-center gap-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onConfirm?.("no")}
            >
              No
            </Button>
            <Button size="sm" onClick={() => onConfirm?.("yes")}>
              Yes
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
export default ConfirmDialog;
