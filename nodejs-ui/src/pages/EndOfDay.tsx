import { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { ArrowLeft, CheckCircle } from "lucide-react";
import { agentAppEndMyDay } from "@/configs/requests/order-service";
import { useTaskStore } from "@/hooks/useTasks";
import Layout from "@/components/molecules/layout";
import { useToast } from "@/hooks/use-toast";
import BankDepositDetailCard, {
  PaymentMethods,
} from "@/components/templates/endOfDay/bankDepositDetailCard";
import DaySummary from "@/components/templates/endOfDay/daySummary";
import CollectionBreakdown from "@/components/templates/endOfDay/collectionBreakdown";
import { useMutation } from "@tanstack/react-query";
import { createDepositSlipParentCashTxnAsync } from "@/configs/requests/start-end.service";

const EndOfDay = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { task_date, day_task_id, deposit_slip_task_id } = location.state || {};
  const overallCollectionData = useTaskStore(
    (state) => state.overallCollectionData
  );
  const agentCashCollectedAmount =
    overallCollectionData?.["agent-cash-collected-amount"];
  const hasCollectionDetails =
    agentCashCollectedAmount?.collection_details?.length > 0;
  const cash_collected_amount = agentCashCollectedAmount?.cash_collected_amount;
  const cashAmount = Number(
    agentCashCollectedAmount?.collection_grouped_values?.cash
  );
  const dashbordData = useTaskStore((state) => state.dashbordData);
  const [notes, setNotes] = useState("");

  const { toast } = useToast();

  const [deposits, setDeposits] = useState([]);

  const totalAddedDepositAmount = useMemo(
    () => deposits?.map((m) => Number(m.amount)).reduce((a, b) => a + b, 0),
    [deposits]
  );
  const depositedError =
    totalAddedDepositAmount < cashAmount
      ? `Deposited amount is less than ₹${cashAmount}`
      : totalAddedDepositAmount > cashAmount
      ? `Deposited amount is more than ₹${cashAmount}`
      : undefined;

  const enabledUploadDepositSlip =
    dashbordData?.enable_upload_deposit_slip && hasCollectionDetails;

  const {
    mutateAsync: createDepositMutateAsync,
    isSuccess: createDepositSuccess,
    isPending: createDepositPending,
  } = useMutation({
    mutationKey: ["createDepositSlipParentCashTxnAsync"],
    mutationFn: createDepositSlipParentCashTxnAsync,
  });

  const {
    mutateAsync: endDayMutateAsync,
    isSuccess: endDaySuccess,
    isPending: endDayPending,
  } = useMutation({
    mutationKey: ["agentAppEndMyDay"],
    mutationFn: agentAppEndMyDay,
  });

  const handleSubmitReport = () => {
    if ((!deposits || deposits.length <= 0) && enabledUploadDepositSlip) {
      toast({
        title: "Invalid",
        description: "Please add atleast deposit slip to proceed",
        variant: "destructive",
      });
      return;
    }
    const anyFieldMissing = deposits?.some(
      (depositItem) =>
        !depositItem.amount ||
        !depositItem.slipNumber ||
        !depositItem.file ||
        !depositItem.type
    );
    if (anyFieldMissing) {
      toast({
        title: "Invalid",
        description: "Please fill all the mandatory deposit details",
        variant: "destructive",
      });
      return;
    }

    const payload = {
      request: {
        task_id: day_task_id,
        deposit_slip: hasCollectionDetails,
        deposit_amount: cash_collected_amount,
        additional_info: [
          {
            comment: notes,
          },
        ],
      },
    };
    endDayMutateAsync(payload).then((res) => {
      if (enabledUploadDepositSlip) {
        const refId = res?.depositSlipTaskId;
        const depositPayload = {
          input: {
            transactions_list: deposits?.map((depositItem) => ({
              from: { user_id: 1, user_name: "Ninjacart" },
              to: {
                user_id: PaymentMethods.find(
                  (m) => m.value === depositItem.type
                ).id,
                user_name: depositItem.type,
              },
              ref_id: refId,
              amount: Number(depositItem.amount),
              utr_number: depositItem.slipNumber,
              image_url: depositItem.file,
            })),
          },
        };

        createDepositMutateAsync(depositPayload);
      }
    });
  };

  const handleSaveAsDraft = () => {
    navigate("/dashboard");
  };

  if (
    (!enabledUploadDepositSlip && endDaySuccess) ||
    (enabledUploadDepositSlip && createDepositSuccess && endDaySuccess)
  ) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <Card className="text-center max-w-md mx-auto">
          <CardContent className="p-8">
            <CheckCircle className="w-16 h-16 text-success mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-success mb-2">
              Day Completed!
            </h2>
            <p className="text-muted-foreground mb-4">
              Your end-of-day report has been submitted successfully. Thank you
              for your hard work today!
            </p>
            <div className="bg-muted p-4 rounded-lg">
              <p className="text-sm font-semibold">Summary:</p>
              {/* <p className="text-sm">Tasks: {countTasksByStatus(dashbordData,'COMPLETED')}/{countTasksByStatus(dashbordData,'PENDING')}</p> */}
              <p className="text-sm">
                Collections: ₹{cash_collected_amount.toLocaleString()}
              </p>
            </div>
            <div className="mt-2">
              <Button
                variant="default"
                size="sm"
                onClick={() => {
                  navigate("/dashboard");
                }}
              >
                Go to Dashboard
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <Layout
      headerContent={
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate("/dashboard")}
            className="text-white hover:bg-white/20"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-lg font-semibold">End of Day Report</h1>
            <p className="text-white/80 text-sm">
              {new Date().toLocaleDateString("en-IN", {
                weekday: "long",
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </p>
          </div>
        </div>
      }
      footerContent={
        <div className="space-y-3">
          <Button
            size="mobile"
            className="w-full"
            onClick={handleSubmitReport}
            disabled={
              createDepositPending ||
              endDayPending ||
              !day_task_id ||
              (!!depositedError && enabledUploadDepositSlip)
            }
          >
            <CheckCircle className="w-4 h-4 mr-2" />
            Submit End of Day Report
          </Button>

          {/* <Button
          variant="outline"
          size="mobile"
          className="w-full"
          onClick={handleSaveAsDraft}
        >
          Save as Draft
        </Button> */}
        </div>
      }
    >
      {/* Day Summary */}
      <DaySummary />

      {/* Collections Breakdown */}
      <CollectionBreakdown />

      {/* Cash Deposit Section */}
      {enabledUploadDepositSlip && (
        <>
          <BankDepositDetailCard onChangeDeposits={setDeposits} />
          {depositedError && (
            <span className="text-sm text-destructive">{depositedError}</span>
          )}
        </>
      )}

      {/* Additional Notes */}
      <Card>
        <CardHeader>
          <CardTitle>
            <h1 className="text-lg sm:text-xl lg:text-2xl font-semibold text-foreground">
              Additional Notes
            </h1>
          </CardTitle>
          <CardDescription>
            Any issues, observations, or comments from today
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Textarea
            placeholder="Enter any notes about today's activities..."
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            rows={4}
          />
        </CardContent>
      </Card>
    </Layout>
  );
};

export default EndOfDay;
