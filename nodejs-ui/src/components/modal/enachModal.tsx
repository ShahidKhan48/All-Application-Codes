import { useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

interface ENachMandatePopupProps {
  open: boolean
  onClose: () => void
  onCreate: (amount: number) => void
  storeName: string
}

export function ENachMandatePopup({ open, onClose, onCreate, storeName }: ENachMandatePopupProps) {
  const [amount, setAmount] = useState("")

  const handleCreate = () => {
    if (!amount) return
    onCreate(Number(amount))
    setAmount("")
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md rounded-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {/* <img src="/logo.png" alt="E-Nach" className="h-6" /> */}
            <span className="text-lg font-semibold">E-Nach</span>
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <Label className="text-gray-600">Store Name</Label>
            <p className="font-medium">{storeName}</p>
          </div>

          <div>
            <Label>Enter amount to create mandate</Label>
            <Input
              type="number"
              placeholder="Enter amount"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
          </div>
        </div>

        <DialogFooter className="flex justify-end gap-2">
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
          <Button onClick={handleCreate}>Create</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
