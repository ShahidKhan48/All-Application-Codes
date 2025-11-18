import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
  DrawerClose,
} from '@/components/ui/drawer';
import { Button } from '@/components/ui/button';
import { useEffect } from 'react';
import { fetchCashTxnStatusAsync } from '@/configs/requests/collection-service';

export function QrDrawer({ open, onClose, qrData }) {
  if (!qrData) return null;

  useEffect(() => {
    let interval: NodeJS.Timeout;

    if (open && qrData?.id) {
      interval = setInterval(async () => {
        try {
          const res = await fetchCashTxnStatusAsync(qrData.id);
          if (res?.transaction_status === 'APPROVED') {
            clearInterval(interval);
            onClose(false); // close modal
          }
        } catch (err) {
          console.error('Error fetching txn status:', err);
        }
      }, 5000); // poll every 5 sec
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [open, qrData?.id, onClose]);

  return (
    <Drawer open={open} onOpenChange={onClose}>
      <DrawerContent className="rounded-t-2xl p-4 sm:p-6">
        <DrawerHeader>
          <DrawerTitle>Scan QR Code</DrawerTitle>
          <DrawerDescription>
            This QR will expire in{' '}
            <span className="font-medium text-destructive">
              {qrData.expiry} mins
            </span>
          </DrawerDescription>
        </DrawerHeader>

        <div className="flex justify-center py-4">
          <img
            src={qrData.image}
            alt="QR Code"
            className="w-48 h-48 rounded-lg border"
          />
        </div>

        <div className="flex justify-center pb-4">
          <DrawerClose asChild>
            <Button variant="outline">Close</Button>
          </DrawerClose>
        </div>
      </DrawerContent>
    </Drawer>
  );
}
