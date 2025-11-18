import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { MapPin } from "lucide-react";
import { Task, TaskStatusEnum, TaskTypeEnum } from "@/lib/dashborad-type";
import { getExpiresOn, getTodayDate } from "@/lib/utils";
import { useTaskStore } from "@/hooks/useTasks";
import { ENachMandatePopup } from "@/components/modal/enachModal";
import { useMutation } from "@tanstack/react-query";
import { runEnachMandateAsync } from "@/configs/requests/collection-service";
import ScreenHeader from "@/components/molecules/screen-header";
import { TaskCard } from "@/components/molecules/task-card";
import { useToast } from "@/hooks/use-toast";
import { DeliveryVerificationDrawer } from "@/components/ui/DeliveryVerificationDrawer";
import { sendOtpAsync, verifyOtpAsync } from "@/configs/requests/login-service";
import {
  closeAgentTaskAsync,
  dashboardApiAsync,
} from "@/configs/requests/dashbord-service";
import { useBrowserCoordinates } from "@/hooks/useBrowserCoordinates";
import Layout from "@/components/molecules/layout";
import {
  agentTaskUpdateAsync,
  cancelAgentTaskAsync,
  getEmandateStatusAsync,
} from "@/configs/requests/tasks-services";
import WebView from "@/components/molecules/webView";
import { ENachDrawer } from "@/components/ui/enach-drawer";
import ConfirmDialog from "@/components/ui/confirm";

const StoreTasks = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { lat, lng } = useBrowserCoordinates();
  const {
    selectedTask,
    setSelectedTask,
    setDashbordData,
    slectedStoreInfo,
    setSelectedStoreInfo,
  } = useTaskStore((state) => state);

  const { mutateAsync: runEnachMandateMutate } = useMutation({
    mutationKey: ["runEnachMandateAsync"],
    mutationFn: runEnachMandateAsync,
  });
  const { mutateAsync: dashboardApiMutate, isPending } = useMutation({
    mutationKey: ["dashboardApiAsync"],
    mutationFn: dashboardApiAsync,
  });
  const { mutateAsync: cancelTaskMutate, isPending: isCancelingTask } =
    useMutation({
      mutationKey: ["cancelAgentTaskAsync"],
      mutationFn: cancelAgentTaskAsync,
    });
  const { mutateAsync: closeTaskMutate, isPending: isClosingTask } =
    useMutation({
      mutationKey: ["closeAgentTaskAsync"],
      mutationFn: closeAgentTaskAsync,
    });

  const { mutateAsync: agentTaskUpdateMutate, isPending: isUpdatingTask } =
    useMutation({
      mutationKey: ["agentTaskUpdateAsync"],
      mutationFn: agentTaskUpdateAsync,
    });

  const [showEnach, setShowEnach] = useState(false);
  const [toggleDeliveryVerification, setToggleDeliveryVerification] =
    useState(false);
  const [enachConfig, setEnachConfig] = useState({
    shoewDrawer: false,
    link: "",
  });
  const [showDailogue, setShowDialogue] = useState({
    type: "",
    payload: null,
    isToggle: false,
  });
  // const displayStoreName = decodeURIComponent(storeName || stateStoreName || '');

  const initateTask = async (task: Task) => {
    const data = await agentTaskUpdateMutate({
      taskId: task?.id,
      setStartTime: true,
      location: {
        lat: String(lat),
        lon: String(lng),
      },
    });

    if (data) {
      const updatedTask: Task = { ...task, status: "IN_PROGRESS" as const };
      setSelectedStoreInfo({
        ...slectedStoreInfo,
        tasks: slectedStoreInfo?.tasks.map((item) =>
          item.id === task.id ? updatedTask : item
        ),
      });
      onTaskClick(updatedTask);
    }
  };

  const onTaskClick = (task) => {
    setSelectedTask(task);
    if(task.status === TaskStatusEnum.COMPLETED && task.task_type !== TaskTypeEnum.DUE_COLLECTION){
      return
    }
    if (task.status === TaskStatusEnum.PENDING) {
      initateTask(task);
    } else if (task.status === TaskStatusEnum.IN_PROGRESS) {
      if (task.task_type === TaskTypeEnum.STORE_AUDIT) {
        handleStoreAuditTask(task);
      } else if (task.task_type === TaskTypeEnum.DUE_COLLECTION) {
        fetchCashCollections(task);
      } else if (task.task_type === TaskTypeEnum.DELIVERY) {
        handleDeliveryTasks(task);
      } else if (task.task_type === TaskTypeEnum.DELIVERY_VERIFICATION) {
        handleDeliveryVerification();
      } else if (task.task_type === TaskTypeEnum.ENACH) {
        handleENachTask(task);
      } else {
        // default case
      }
    } else if (
      task.status === TaskStatusEnum.COMPLETED &&
      task?.task_type === TaskTypeEnum.DUE_COLLECTION
    ) {
      fetchCashCollections(task);
    } else {
      // default status case
    }
  };

  const handleStoreAuditTask = (task) => {
    if (!task?.warehouse_id) {
      toast({
        title: "Invalid",
        description: "Invalid warehouse",
        variant: "destructive",
        autoFocus: true,
      });
      return;
    }
    navigate("/sales-order", {
      state: { task, customer: {} },
    });
  };

  const [deliveryWebViewConfig, setDeliveryWebViewConfig] = useState<{
    orderId: string;
    show: boolean;
  }>({} as any);
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
        description: `Order ID:${deliveryWebViewConfig?.orderId} has been delivered successfully`,
      });
      handleCloseTask(selectedTask?.id);
    }
    if (close === true) {
      setDeliveryWebViewConfig({} as any);
    }
  };

  if (deliveryWebViewConfig?.show === true && omniTradeUrl) {
    return (
      <WebView
        src={`${omniTradeUrl}/wholesale/orders`}
        onClose={handleWebViewClose}
        additionalInfo={{
          agentDeliverOrderId: deliveryWebViewConfig.orderId,
        }}
      />
    );
  }

  // Delivery Tasks
  const handleDeliveryTasks = async (task) => {
    if (!task?.order_id) {
      toast({
        title: "Invalid",
        description: "Invalid Order ID",
        variant: "destructive",
      });
      return;
    }
    setDeliveryWebViewConfig({
      show: true,
      orderId: task?.order_id,
    });
  };

  // Fetch Cash Collections
  const fetchCashCollections = async (task) => {
    try {
      navigate("/collections");
    } catch (err) {
      console.error("Error in fetchCashCollections:", err);
    }
  };

  // Delivery Verification
  const handleDeliveryVerification = () => {
    //yet to implemented
    return;
  };

  // E-Nach Task
  const handleENachTask = async (task) => {
    console.log(task);
    if (!task?.additional_info?.url) {
      setShowEnach(true);
    } else {
      const mandateStatusStream = await getEmandateStatusAsync(
        task.additional_info.mandateId,
        slectedStoreInfo?.store_id,
        slectedStoreInfo?.store_realm_id
      );
      if (mandateStatusStream?.status === "ACTIVE") {
        handleCloseTask(task?.id);
      } else {
        setShowEnach(true);
      }
    }
  };

  const runEnachMandate = async (amount) => {
    const data = await runEnachMandateMutate({
      input: {
        userName: slectedStoreInfo?.store_name,
        mobile: slectedStoreInfo?.contact_number,
        referenceId: slectedStoreInfo?.store_id?.toString(),
        taskId: selectedTask?.id?.toString(),
        expiresOn: getExpiresOn(),
        enach_max_amount: amount,
      },
    });
    if (data) {
      setShowEnach(false);
      setEnachConfig({
        shoewDrawer: true,
        link: data?.url,
      });
    }
  };

  const filteredTasks = slectedStoreInfo?.tasks?.filter(
    (m) => m.status !== "CANCELLED" && m.task_type !== "STORE_VISIT"
  );

  const handleCloseTask = async (id) => {
    closeTaskMutate({
      input: {
        task_id: String(selectedTask?.id),
        additional_info: [
          { comments: "Task Delivered" },
          { reason: "Task Delivered" },
        ],
      },
    })
      .then(() => {
        setSelectedStoreInfo({
          ...slectedStoreInfo,
          tasks: slectedStoreInfo?.tasks.map((item) =>
            item.id === id ? { ...item, status: "COMPLETED" } : item
          ),
        });
      })
      .catch((e) => {
        toast({
          title: "Failed",
          description:
            e?.errorMessage ||
            e?.message ||
            "Something went wrong while closing task",
          variant: "destructive",
        });
      })
      .finally(() => {
        setShowDialogue({
          isToggle: false,
          payload: null,
          type: "",
        });
      });
  };

  // ✅ Cancel Task Function
  const handleCancelTask = async () => {
    cancelTaskMutate(showDailogue?.payload)
      .then(() => {
        setSelectedStoreInfo({
          ...slectedStoreInfo,
          tasks: slectedStoreInfo?.tasks.map((item) =>
            item.id === showDailogue.payload
              ? { ...item, status: "CANCELLED" }
              : item
          ),
        });
      })
      .catch((e) => {
        toast({
          title: "Failed",
          description:
            e?.errorMessage ||
            e?.message ||
            "Something went wrong while cancel task",
          variant: "destructive",
        });
      })
      .finally(() => {
        setShowDialogue({
          isToggle: false,
          payload: null,
          type: "",
        });
      });
  };

  const onConfirm = async (res) => {
    if (res === "no") {
      setShowDialogue({
        isToggle: false,
        payload: null,
        type: "",
      });
      return;
    }
    if (showDailogue?.type === "CLOSE") {
      await handleCloseTask(showDailogue?.payload);
    } else {
      await handleCancelTask();
    }
  };

  return (
    <>
      <Layout
        headerContent={
          <ScreenHeader
            title={slectedStoreInfo?.store_name}
            onBack={() => navigate("/dashboard")}
          />
        }
      >
        {slectedStoreInfo?.tasks?.[0]?.total_travel_km && (
          <div className="flex items-center gap-1 text-sm">
            <MapPin className="w-3 h-3" />
            <span>{slectedStoreInfo?.tasks?.[0]?.total_travel_km}</span>
          </div>
        )}
        <div>
          <h2 className="text-lg font-semibold text-left">Available Tasks</h2>
          <p className="text-sm text-left">Select a task to begin</p>
        </div>

        <div className="flex flex-col gap-3">
          {filteredTasks?.map((task) => (
            <TaskCard
              key={task?.id}
              id={task?.id?.toString()}
              orderId={task?.order_id?.toString()}
              type={task?.task_type}
              status={task?.status}
              onAction={() => onTaskClick(task)}
              isPending={isCancelingTask || isClosingTask || isUpdatingTask}
              onClose={() =>
                setShowDialogue({
                  isToggle: true,
                  payload: task?.id,
                  type: "CLOSE",
                })
              }
              onCancelTask={(id) =>
                setShowDialogue({
                  isToggle: true,
                  payload: task?.id,
                  type: "CANCEL",
                })
              }
            />
          ))}
        </div>
      </Layout>
      <ENachMandatePopup
        open={showEnach}
        onClose={() => setShowEnach(false)}
        onCreate={(amount) => runEnachMandate(amount)}
        storeName={slectedStoreInfo?.store_name}
      />
      <DeliveryVerificationDrawer
        open={toggleDeliveryVerification}
        primaryPhoneNumber={slectedStoreInfo?.contact_number}
        phoneNumbersList={slectedStoreInfo?.store_contact_numbers}
        onDismiss={() => setToggleDeliveryVerification(false)}
        onCancelTask={() => setToggleDeliveryVerification(false)}
        onSendOtp={sendOtpAsync}
        onValidate={verifyOtpAsync}
      />
      <ENachDrawer
        open={enachConfig?.shoewDrawer}
        setOpen={() => setEnachConfig({ shoewDrawer: false, link: "" })}
        customerName={slectedStoreInfo?.store_name}
        url={enachConfig?.link}
      />
      {showDailogue?.isToggle && (
        <ConfirmDialog
          open={showDailogue?.isToggle}
          onOpenChange={() =>
            setShowDialogue({
              isToggle: false,
              payload: null,
              type: "",
            })
          }
          onConfirm={onConfirm}
        >
          <div className="text-center">
            {showDailogue?.type === "CANCEL"
              ? "Are you sure to Cancel this Task?"
              : "Are you sure to Close this Task?"}
          </div>
        </ConfirmDialog>
      )}
    </>
  );
};

export default StoreTasks;
