import PaymentIcon from "@/components/molecules/payment-icon";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useTaskStore } from "@/hooks/useTasks";
import { CreditCard } from "lucide-react";
import { useMemo } from "react";

const CollectionBreakdown = () => {
  const overallCollectionData = useTaskStore((m) => m.overallCollectionData);
  const agentCashCollectedAmount =
    overallCollectionData?.["agent-cash-collected-amount"];
  const collectionDetails =
    overallCollectionData?.["agent-cash-collected-amount"]?.collection_details;

  const PaymentModeData = useMemo(
    () =>
      collectionDetails?.reduce((prev, curr) => {
        const currMode = curr["payment_mode"];
        const currAmount = curr["amount"];
        prev[currMode] = (
          prev[currMode] ? prev[currMode] + currAmount : currAmount
        ) as number;
        return prev;
      }, {}),
    [collectionDetails]
  );

  const PaymentModeValues = Object.entries(PaymentModeData);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <CreditCard className="w-5 h-5" />
          <h1 className="text-lg sm:text-xl lg:text-2xl font-semibold text-foreground">
            Collections Breakdown
          </h1>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid  gap-4 text-center">
          <div className="p-3 bg-warning/10 rounded-lg">
            <p className="text-lg font-bold text-warning">
              ₹{agentCashCollectedAmount?.cash_collected_amount}
            </p>
            <p className="text-sm text-muted-foreground">Total Collections</p>
          </div>
        </div>

        {PaymentModeValues?.length > 0 && (
          <div className="space-y-3">
            <h1 className="text-lg sm:text-xl lg:text-2xl font-semibold text-foreground">
              Payment Method Breakdown
            </h1>
            {PaymentModeValues?.map((item, index) => (
              <div
                key={index + 1}
                className="flex items-center justify-between p-2 bg-muted rounded"
              >
                <div className="flex items-center gap-2">
                  <PaymentIcon method={item?.[0]} />
                  <span className="capitalize font-medium">{item?.[0]}</span>
                </div>
                <span className="font-semibold">₹{item?.[1] as any}</span>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
};
export default CollectionBreakdown;
