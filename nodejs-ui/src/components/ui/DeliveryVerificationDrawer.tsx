import React, { useState } from 'react';
import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerFooter,
} from '@/components/ui/drawer';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  OtpRequestPayload,
  OtpVerifyPayload,
} from '@/configs/requests/login-service';
import { closeAgentTaskAsync } from '@/configs/requests/dashbord-service';

interface DeliveryVerificationDrawerProps {
  open: boolean;
  primaryPhoneNumber: string;
  phoneNumbersList: string[];
  onDismiss: () => void;
  onCancelTask: () => void;
  onSendOtp: (payload: OtpRequestPayload) => Promise<void> | void;
  onValidate: (payload: OtpVerifyPayload) => Promise<void> | void;
}

export function DeliveryVerificationDrawer({
  open,
  primaryPhoneNumber,
  phoneNumbersList,
  onDismiss,
  onCancelTask,
  onSendOtp,
  onValidate,
}: DeliveryVerificationDrawerProps) {
  const [secondaryPhoneNumber, setSecondaryPhoneNumber] = useState('');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSendOtp = async () => {
    setLoading(true);
    await onSendOtp({
      input: {
        identity: primaryPhoneNumber,
        otpRequestType: 'NINJA_APP_LOGIN',
        identityType: 'PHONE_NUMBER',
        captchaText: null,
      },
    });
    setLoading(false);
  };

  const handleValidate = async () => {
    setLoading(true);
    await onValidate({
      input: {
        identity: primaryPhoneNumber,
        otp: otp,
        identityType: 'PHONE_NUMBER',
        otpRequestType: 'NINJA_APP_LOGIN',
      },
    });
    setLoading(false);
  };

  return (
    <Drawer open={open} onOpenChange={onDismiss}>
      <DrawerContent className="p-6 space-y-4 max-h-[90vh] overflow-y-auto">
        <DrawerHeader>
          <DrawerTitle>Delivery Verification</DrawerTitle>
        </DrawerHeader>

        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium">Primary Phone</label>
            <Input value={primaryPhoneNumber} readOnly />
          </div>

          <div>
            <label className="text-sm font-medium">Secondary Phone</label>
            <Input
              value={secondaryPhoneNumber}
              onChange={(e) => setSecondaryPhoneNumber(e.target.value)}
              placeholder="Enter secondary number"
            />
          </div>

          <div>
            <label className="text-sm font-medium">OTP</label>
            <Input
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              placeholder="Enter OTP"
            />
          </div>
        </div>

        <DrawerFooter className="flex justify-between gap-2">
          <Button variant="outline" onClick={onCancelTask}>
            Cancel Task
          </Button>
          <Button onClick={handleSendOtp} disabled={loading}>
            {loading ? 'Sending...' : 'Send OTP'}
          </Button>
          <Button onClick={handleValidate} disabled={loading}>
            {loading ? 'Validating...' : 'Validate'}
          </Button>
        </DrawerFooter>
      </DrawerContent>
    </Drawer>
  );
}
