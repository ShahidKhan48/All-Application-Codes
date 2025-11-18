import { AgentTaskResponse, IDashboardData, Task } from '@/lib/dashborad-type';
import { IAgentAppEndMyDayReport } from '@/lib/end-my-day-types';
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface IMileStoneData {
  slectedStoreInfo: AgentTaskResponse | null;
  setSelectedStoreInfo: (data: AgentTaskResponse) => void;
  selectedTask: Task | null;
  setSelectedTask: (data: Task) => void;
  overallCollectionData : IAgentAppEndMyDayReport | null
  setOverallCollectionData: (data : IAgentAppEndMyDayReport) => void
  dashbordData : IDashboardData | null;
  setDashbordData : (data : IDashboardData) => void
}

export const useTaskStore = create<IMileStoneData>()(
  persist(
    (set) => ({
      slectedStoreInfo: null,
      setSelectedStoreInfo: (data: AgentTaskResponse) =>
        set({ slectedStoreInfo: data }),
      selectedTask: null,
      setSelectedTask: (data: Task) => set({ selectedTask: data }),
      overallCollectionData : null,
      setOverallCollectionData: (data : IAgentAppEndMyDayReport) => set({overallCollectionData : data}),
      dashbordData : null,
      setDashbordData: (data : IDashboardData) => set({dashbordData : data})
    }),
    {
      name: 'task-modal-store', // Key for localStorage
    }
  )
);
