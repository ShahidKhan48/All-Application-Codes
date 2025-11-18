import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
  DrawerFooter,
  DrawerClose,
} from "@/components/ui/drawer";
import { Button } from "@/components/ui/button";
import { X } from "lucide-react";
import { useState } from "react";

export function ENachDrawer({ customerName ,url, open, setOpen}: { customerName: string,url: string, open : boolean, setOpen : () => void}) {

  const handleCopy = () => {
    navigator.clipboard.writeText(url);
    alert("Link copied!");
  };

  return (
    <Drawer open={open} onOpenChange={setOpen}>
      <DrawerContent className="rounded-t-2xl p-4 sm:p-6">
        {/* Header with close button */}
        <DrawerHeader className="flex justify-between items-center pb-2">
          <DrawerTitle className="text-lg font-semibold">E-Nach 📝</DrawerTitle>
          <DrawerClose asChild>
            <Button
              variant="ghost"
              size="icon"
              className="rounded-full hover:bg-muted"
            >
              <X className="h-4 w-4" />
            </Button>
          </DrawerClose>
        </DrawerHeader>

        {/* Description */}
        <DrawerDescription className="text-sm text-muted-foreground mb-4">
          Please select a method to share the verification link for the
          customer <span className="font-medium">{customerName}</span>
        </DrawerDescription>

        {/* Footer with action */}
        <DrawerFooter>
          <Button
            onClick={handleCopy}
            className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
          >
            Copy Link
          </Button>
        </DrawerFooter>
      </DrawerContent>
    </Drawer>
  );
}
