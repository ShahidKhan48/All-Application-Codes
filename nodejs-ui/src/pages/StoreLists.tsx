import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ArrowLeft, ShoppingBag } from "lucide-react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  createAgentTaskAsync,
  fetchAgentFacilityAsync,
} from "@/configs/requests/create-task";
import { getUserIds } from "@/lib/storage";
import { useEffect, useState } from "react";
import { useToast } from "@/hooks/use-toast";
import Spinner from "@/components/ui/spinner";
import { dateFormat } from "@/lib/date";
import Layout from "@/components/molecules/layout";
import { useTaskStore } from "@/hooks/useTasks";

const StoreLists = () => {
  const navigate = useNavigate();
  const [stores, setStores] = useState<any[]>([]);
  const { toast } = useToast();
  const dashbordData = useTaskStore((m) => m.dashbordData);

  const { data, isLoading, isFetching, isRefetching } = useQuery({
    queryKey: ["FetchAgentFacilityRequestProps"],
    queryFn: async () => {
      let payload = {
        input: {
          statuses: ["ASSIGNED"],
          agentIds: [getUserIds()?.systemUserId],
          agentTypes: ["STORE_AUDIT"],
          facilityIds: [],
          facilityTypes: ["STORE"],
          page: 0,
          size: 100,
        },
      };
      const response = await fetchAgentFacilityAsync(payload);
      const { facilities } = response || {};
      const unselectedFacilities = facilities?.map((facilityItem) => ({
        ...facilityItem,
        selected: false,
      }));
      return {
        facilities: unselectedFacilities,
      };
    },
  });

  const { mutateAsync, isPending } = useMutation({
    mutationKey: ["createAgentTaskAsync"],
    mutationFn: createAgentTaskAsync,
    onError: (e: any) => {
      toast({
        title: "Failed",
        description: e?.message || e?.errorMessage || "Task Creation Failed",
        variant: "destructive",
      });
    },
  });

  useEffect(() => {
    const { facilities } = data || {};
    setStores(facilities);
  }, [data]);

  const noStoreSelected = stores?.every((m) => !m.selected);

  const handleCreateTask = () => {
    if (noStoreSelected) {
      toast({
        description: "Please select any store to proceed",
        variant: "destructive",
      });
      return;
    }
    const selectedStores = stores.filter((m) => m.selected);
    // api call
    const payload = {
      input: selectedStores?.map((storeItem) => ({
        agent_user_id: storeItem?.agent_id,
        store_id: storeItem?.facility_id,
        task_type: storeItem?.agent_type,
        task_date: dateFormat(Date.now(), "yyyy-MM-dd"),
        status: "PENDING",
      })),
    };
    mutateAsync(payload).then(() => {
      toast({
        title: "Success",
        description: "Task Creation Success",
      });
      navigate("/dashboard");
    });
  };

  const StoreCard = ({ store }: { store: any }) => {
    const findStore = dashbordData?.agent_task_response?.find(
      (m) => m.store_id === store.facility_id
    );
    const { tasks = [] } = findStore || {};
    const storeAuditAlreadyCreated = tasks?.some(
      (taskItem) => taskItem.task_type === "STORE_AUDIT"
    );
    return (
      <Card
        key={store?.facility_id}
        className={`cursor-pointer hover:shadow-md transition-shadow ${
          storeAuditAlreadyCreated ? "opacity-50 pointer-events-none" : ""
        }`}
      >
        <CardContent className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-primary/10 rounded-lg flex items-center justify-center">
              <ShoppingBag className="w-5 h-5 text-primary" />
            </div>

            <div className="flex-1 flex flex-col items-start justify-start">
              <h6 className="font-semibold line-clamp-2 text-sm text-left">
                {store?.facility_name}
              </h6>
            </div>
            <input
              type="checkbox"
              className="w-5 h-5"
              checked={store.selected}
              onChange={(e) => {
                setStores((prev) =>
                  prev?.map((m) => ({
                    ...m,
                    selected:
                      m.facility_name === store.facility_name
                        ? e.target.checked
                        : m.selected,
                  }))
                );
              }}
            />
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <Layout
      headerContent={
        <div className="flex items-center gap-3 mb-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate("/dashboard")}
            className="text-white hover:bg-white/20"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-xl font-semibold">Store Lists</h1>
            <p className="text-white/80 text-sm">
              Select a store to create tasks
            </p>
          </div>
        </div>
      }
      footerContent={
        stores?.length > 0 && (
          <div className="flex items-center justify-center">
            <Button
              disabled={noStoreSelected || isPending}
              onClick={handleCreateTask}
            >
              Create Task
            </Button>
          </div>
        )
      }
    >
      {isLoading || isRefetching || isFetching ? (
        <Spinner />
      ) : (
        <div className="p-4 flex-1 overflow-auto">
          {stores?.length > 0 ? (
            <div className="space-y-3">
              {stores?.map((store) => (
                <StoreCard key={store.facility_id} store={store} />
              ))}
            </div>
          ) : (
            <div className="text-center font-[600] text-lg">No data found</div>
          )}
        </div>
      )}
    </Layout>
  );
};

export default StoreLists;
