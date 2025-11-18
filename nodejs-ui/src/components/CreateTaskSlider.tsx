
import React from 'react';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';

interface CreateTaskSliderProps {
  isOpen: boolean;
  onClose: () => void;
  onCreateTask: () => void;
}

export const CreateTaskSlider = ({ isOpen, onClose, onCreateTask }: CreateTaskSliderProps) => {
  return (
    <>
      {/* Overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black/20 z-40"
          onClick={onClose}
        />
      )}
      
      {/* Slider */}
      <div className={`
        fixed bottom-24 right-6 z-50 
        transform transition-transform duration-300 ease-in-out
        ${isOpen ? 'translate-x-0' : 'translate-x-full'}
      `}>
        <div className="bg-background border border-border rounded-lg shadow-lg p-3 min-w-[120px]">
          <Button
            variant="ghost"
            className="w-full justify-start gap-2 text-left"
            onClick={onCreateTask}
          >
            <Plus className="w-4 h-4" />
            Create Task
          </Button>
        </div>
      </div>
    </>
  );
};
