import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Calendar,
  User,
  CreditCard,
  UserPlus,
  Clock,
  Plus,
  X,
  ListOrdered,
  RefreshCcwIcon,
} from "lucide-react";
import { useMutation } from "@tanstack/react-query";
import {
  closeAgentTaskAsync,
  dashboardApiAsync,
} from "@/configs/requests/dashbord-service";
import { AgentTaskResponse, IDashboardData, Task } from "@/lib/dashborad-type";
import { getTodayDate } from "@/lib/utils";
import { useTaskStore } from "@/hooks/useTasks";
import WebView from "@/components/molecules/webView";
import { useToast } from "@/hooks/use-toast";
import { fetchStoreOutstandingAmtAsync } from "@/configs/requests/order-service";
import { getUserIds, getUserRealmInfo } from "@/lib/storage";
import { IAgentAppEndMyDayReport } from "@/lib/end-my-day-types";
import Spinner from "@/components/ui/spinner";
import { useBrowserCoordinates } from "@/hooks/useBrowserCoordinates";
import Layout from "@/components/molecules/layout";
import { agentAppStartDayAsync } from "@/configs/requests/start-end.service";
import { dateFormat } from "@/lib/date";
import { agentTaskUpdateAsync } from "@/configs/requests/tasks-services";
import useDisableBackButton from "@/hooks/useDisableBackButton";
import ConfirmDialog from "@/components/ui/confirm";
import { StoreCard } from "@/components/templates/dashboard/storeCard";

const Dashboard = () => {
  useDisableBackButton();
  const { toast } = useToast();
  const navigate = useNavigate();
  const { lat, lng, error } = useBrowserCoordinates();
  const setSelectedStoreInfo = useTaskStore(
    (state) => state.setSelectedStoreInfo
  );
  const setOverallCollectionDataInStore = useTaskStore(
    (state) => state.setOverallCollectionData
  );
  const setDashbordDataInStore = useTaskStore((state) => state.setDashbordData);
  // const [showCreateSlider, setShowCreateSlider] = useState(false);
  const [dashbordDetails, setDasbordDetails] =
    useState<IDashboardData | null>();
  const [overallCollectionData, setOverallCollectionData] =
    useState<IAgentAppEndMyDayReport>();
  const agentCashCollectedAmount =
    overallCollectionData?.["agent-cash-collected-amount"];
  const [showConfiramtionPopupConfig, setShowConfiramtionPopupConfig] =
    useState({
      isToggle: false,
      payload: null,
    });

  const { mutateAsync: dashboardApiMutate, isPending } = useMutation({
    mutationKey: ["dashboardApiAsync"],
    mutationFn: dashboardApiAsync,
  });

  const { mutateAsync: outstandingMutateAsync, isPending: outstandingPending } =
    useMutation({
      mutationKey: ["fetchStoreOutstandingAmtAsync"],
      mutationFn: fetchStoreOutstandingAmtAsync,
    });

  const { mutateAsync: startDayMutateAsync, isPending: startDayIsPending } =
    useMutation({
      mutationKey: ["agentAppStartDayAsync"],
      mutationFn: agentAppStartDayAsync,
    });

  const { mutateAsync: closeAgentTaskMutate, isPending: isClosingTask } =
    useMutation({
      mutationKey: ["closeAgentTaskAsync"],
      mutationFn: closeAgentTaskAsync,
    });

  const { mutateAsync: agentTaskUpdateMutate, isPending: isUpdating } =
    useMutation({
      mutationKey: ["agentTaskUpdateAsync"],
      mutationFn: agentTaskUpdateAsync,
    });

  // Group tasks by store/customer

  const handleStoreClick = (storeDetails: AgentTaskResponse) => {
    if (
      storeDetails?.tasks?.find((item) => item?.task_type === "STORE_VISIT")
        ?.status === "PENDING"
    ) {
      return;
    }
    console.log("Store details:", storeDetails);
    setSelectedStoreInfo(storeDetails);
    console.log("Store ID:", storeDetails.store_id);
    navigate(`/store-tasks/${encodeURIComponent(storeDetails?.store_id)}`, {
      state: storeDetails,
    });
  };

 

  const fetchDashboardDetails = async () => {
    const data = await dashboardApiMutate({
      request: {
        start_location: `${lat},${lng}`,
        task_date: getTodayDate(),
      },
    });
    if (data) {
      setDasbordDetails(data);
      fetchStoreOutStandingAmt(data?.deposit_slip_task_id, data?.task_date);
      setDashbordDataInStore(data);
    }
  };

  useEffect(() => {
    if(lat,lng){
      fetchDashboardDetails();
    }
  }, [lat,lng]);

  const countTasksByStatus = (
    dashboard: IDashboardData | undefined,
    status: Task["status"]
  ): number => {
    if (!dashboard) return 0;

    return dashboard.agent_task_response.reduce(
      (count, agentTask) =>
        count +
        agentTask.tasks.filter(
          (task) => task.status === status && task.task_type !== "STORE_VISIT"
        ).length,
      0
    );
  };

  const fetchStoreOutStandingAmt = async (deposit_slip_task_id, task_date) => {
    const data = await outstandingMutateAsync({
      deposit_slip_task_id: deposit_slip_task_id,
      user_id: getUserIds()?.userId,
      task_date: task_date,
    });
    setOverallCollectionData(data);
    setOverallCollectionDataInStore(data);
  };

  const handleEndDay = () =>
    navigate("/end-of-day", {
      state: {
        task_date: dashbordDetails?.task_date,
        day_task_id: dashbordDetails?.day_task_id,
        deposit_slip_task_id: dashbordDetails?.deposit_slip_task_id,
      },
    });

  const handleStartDay = () => {
    const payload = {
      request: {
        start_location: `${lat},${lng}`,
        task_date: dateFormat(Date.now(), "yyyy-MM-dd"),
      },
    };
    startDayMutateAsync(payload).then(() => {
      // refresh
      fetchDashboardDetails();
    });
  };

  const [showOnboardWebView, setShowOnbaordWebView] = useState<boolean>(false);
  const omniTradeUrl = import.meta.env.VITE_OMNI_WEBVIEW_URL;

  const handleWebViewClose = ({
    close,
    success,
  }: {
    close: boolean;
    success?: boolean;
  }) => {
    if (success === true) {
      toast({
        title: "Success",
        description: "Customer created successfully!",
      });
    }
    if (close === true) {
      setShowOnbaordWebView(false);
    }
  };

  if (showOnboardWebView && omniTradeUrl) {
    return (
      <WebView
        src={`${omniTradeUrl}/customers/create`}
        onClose={handleWebViewClose}
      />
    );
  }
  const { enable_start_day_button, enable_end_day_button } =
    dashbordDetails || {};

  const completeStoreVistTask = (item: AgentTaskResponse) => {
    const tasks = item.tasks;
    const id = tasks?.find((item) => item?.task_type === "STORE_VISIT")?.id;
    const payload = {
      taskId: id,
      setStartTime: true,
      location: {
        lat: String(lat),
        lon: String(lng),
      },
    };
    agentTaskUpdateMutate(payload)
      .then(() => {
        setSelectedStoreInfo(item);
        navigate(`/store-tasks/${encodeURIComponent(item?.store_id)}`, {
          state: item,
        });
      })
      .catch((e) => {
        toast({
          title: "Failed",
          description:
            e?.errorMessage ||
            e?.message ||
            "Something went wrong while completing task",
          variant: "destructive",
        });
      });
  };

  const closeTaskAsync = (tasks: Task[]) => {
    const id = tasks?.find((item) => item?.task_type === "STORE_VISIT")?.id;
    let payload = {
      input: {
        task_id: String(id),
        additional_info: [
          {
            comments: "Task Completed",
          },
          {
            reason: "Task Completed",
          },
        ],
      },
    };
    closeAgentTaskMutate(payload)
      .then(() => {
        setShowConfiramtionPopupConfig({ isToggle: false, payload: null });
        fetchDashboardDetails();
      })
      .catch((e) => {
        toast({
          title: "Failed",
          description:
            e?.errorMessage ||
            e?.message ||
            "Something went wrong while ending store",
          variant: "destructive",
        });
      });
  };

  const getStatusColor = (status: string) => {
    const colorMap = {
      COMPLETED: "bg-success text-white",
      CANCELLED: "bg-destructive",
      CLOSED: "bg-destructive",
    };
    return colorMap[status] || "bg-default";
  };

  return (
    <Layout
      headerContent={
        <div className="flex items-center justify-between gap-4">
          <div
            className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center"
            onClick={() => navigate("/profile")}
          >
            <User className="w-5 h-5" />
          </div>
          <div className="flex-1 text-center">
            <div className="text-white/90 text-sm font-bold">
              {new Date().toLocaleDateString("en-IN", {
                weekday: "long",
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </div>
          </div>
          <RefreshCcwIcon size={20} onClick={() => fetchDashboardDetails()} />
        </div>
      }
      footerContent={
        !enable_start_day_button && (
          <div className="flex items-center justify-center">
            <Button
              variant="destructive"
              onClick={handleEndDay}
              size="sm"
              disabled={!enable_end_day_button}
            >
              <Calendar className="w-5 h-5" />
              End of Day
            </Button>
          </div>
        )
      }
    >
      {isPending ? (
        <Spinner label="Please wait while we are fetching details..." />
      ) : (
        <div className="p-4 space-y-6  flex-1 overflow-auto">
          {/* Quick Stats */}
          <div className="grid grid-cols-2 gap-4">
            <Card className="border-l-4 border-l-primary">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">
                      Pending Tasks
                    </p>
                    <p className="text-2xl font-bold text-primary">
                      {countTasksByStatus(dashbordDetails, "PENDING") + countTasksByStatus(dashbordDetails, "IN_PROGRESS")}
                    </p>
                  </div>
                  <Clock className="w-8 h-8 text-primary" />
                </div>
              </CardContent>
            </Card>

            <Card className="border-l-4 border-l-success">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">Collections</p>
                    <p
                      className="text-2xl font-bold text-success truncate max-w-full overflow-hidden"
                      title={`₹${agentCashCollectedAmount?.cash_collected_amount.toLocaleString()}`}
                    >
                      {outstandingPending ? (
                        <Spinner size="sm" />
                      ) : (
                        <>
                          ₹
                          {agentCashCollectedAmount?.cash_collected_amount.toLocaleString()}
                        </>
                      )}
                    </p>
                  </div>
                  <CreditCard className="w-8 h-8 text-success" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Stores List */}
          <div>
            {enable_start_day_button && (
              <Card className="text-center p-8">
                <h3 className="text-lg font-semibold mb-2">
                  Hello {(getUserRealmInfo() as any)?.name}!
                </h3>
                <p className="text-muted-foreground mb-4">
                  Please click on start of Day to proceed
                </p>
                <Button
                  variant="success"
                  onClick={handleStartDay}
                  disabled={startDayIsPending}
                  size="sm"
                >
                  <Calendar className="w-5 h-5" />
                  {startDayIsPending ? "Starting your day..." : "Start of Day"}
                </Button>
              </Card>
            )}
            <div className="text-red-500  text-left">
              *
              {dashbordDetails?.deposit_slip_comment ??
                dashbordDetails?.comment}
            </div>
            {dashbordDetails?.agent_task_response?.length > 0 && (
              <>
                <h2 className="text-xl font-semibold mb-4 text-left">
                  Assigned Stores
                </h2>
                <div className="space-y-3">
                  {dashbordDetails?.agent_task_response?.map((item, index) => (
                    <StoreCard
                      index={index}
                      isClosingTask={isClosingTask}
                      isUpdating={isUpdating}
                      item={item}
                      onStoreClick={handleStoreClick}
                      onCompleteStoreVisit={completeStoreVistTask}
                      onEndStore={(tasks) => {
                        setShowConfiramtionPopupConfig({
                          isToggle: true,
                          payload: tasks,
                        });
                      }}
                    />
                  ))}
                </div>
              </>
            )}
          </div>

          {/* Quick Actions */}
          <div>
            <h2 className="text-xl font-semibold mb-4 text-left">
              Quick Actions
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <Button
                variant="outline"
                size="mobile"
                className="h-16 flex flex-col gap-1"
                onClick={() => navigate("/order-lists")}
              >
                <ListOrdered className="w-5 h-5" />
                <span className="text-xs">Orders</span>
              </Button>

              <Button
                variant="outline"
                size="mobile"
                className="h-16 flex flex-col gap-1"
                onClick={() => setShowOnbaordWebView(true)}
              >
                <UserPlus className="w-5 h-5" />
                <span className="text-xs">Onboarding</span>
              </Button>
            </div>
          </div>

          {/* Floating Create Task Button */}
          {/* <div className="fixed bottom-6 right-6">
            <Button
              size="icon"
              className="w-14 h-14 rounded-full shadow-lg"
              onClick={handleCreateTaskClick}
            >
              <Plus className="w-6 h-6" />
            </Button>
          </div> */}

          {showConfiramtionPopupConfig?.payload && (
            <ConfirmDialog
              open={showConfiramtionPopupConfig.isToggle}
              onOpenChange={() =>
                setShowConfiramtionPopupConfig({
                  isToggle: false,
                  payload: null,
                })
              }
              onConfirm={(res) => {
                setShowConfiramtionPopupConfig({
                  isToggle: false,
                  payload: null,
                });
                if (res === "no") return;
                closeTaskAsync(showConfiramtionPopupConfig?.payload);
              }}
            >
              <div className="text-center">Are you sure to End this Store?</div>
            </ConfirmDialog>
          )}
        </div>
      )}
    </Layout>
  );
};

export default Dashboard;
