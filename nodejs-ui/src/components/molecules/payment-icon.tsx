import { Collection } from "@/data/mockData";
import {
  Banknote,
  Building,
  CreditCard,
  FileText,
  QrCode,
  Link2,
} from "lucide-react";

const PaymentIcon = ({ method }: { method: string }) => {
  const iconMap = {
    CASH: <Banknote className="w-4 h-4" />,
    NEFT: <Building className="w-4 h-4" />,
    UPI: <CreditCard className="w-4 h-4" />,
    QR: <QrCode className="w-4 h-4" />,
    CHEQUE: <FileText className="w-4 h-4" />,
    PAYMENT_LINK: <Link2 className="w-4 h-4" />,
  };
  return iconMap[method] || <CreditCard className="w-4 h-4" />;
};
export default PaymentIcon;
