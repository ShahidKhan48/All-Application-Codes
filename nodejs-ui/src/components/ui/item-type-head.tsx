import { useState } from "react";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Check, ChevronsUpDown } from "lucide-react";
import { cn } from "@/lib/utils";

export interface VendorProduct {
  id: string;
  itemName: string;
  itemId: string;
  unitPrice: number;
  gstPercentage: number;
  cessPercentage?: number;
  hsnCode: string;
  itemUomName: string;
  imageURL?: string;
  mrp?: number;
  rate?: number;
  amount?: number;
}

interface ItemTypeaheadProps {
  onSelect: (product: VendorProduct) => void;
  selectedItem?: VendorProduct | null;
  placeholder?: string;
  products: VendorProduct[];
  disabled?: boolean;
}

export function ItemTypeahead({
  onSelect,
  selectedItem,
  placeholder = "Search items...",
  products,
  disabled,
}: ItemTypeaheadProps) {
  const [open, setOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const filteredProducts = products.filter((product) =>
    product.itemName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  console.log(filteredProducts,"filteredProducts ******")





  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className="w-full justify-between"
          disabled={disabled}
        >
          <span className="truncate text-left">
            {selectedItem ? selectedItem.itemName : placeholder}
          </span>
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[400px] p-0">
        <Command>
          <CommandInput
            placeholder="Search items..."
            value={searchTerm}
            onValueChange={setSearchTerm}
          />
          <CommandList>
            <CommandEmpty>
              {products.length === 0
                ? "No items available. Select a vendor first."
                : "No items found."}
            </CommandEmpty>
            <CommandGroup>
              {filteredProducts.map((product) => (
                <CommandItem
                  key={product.id}
                  value={product.itemName}
                  onSelect={() => {
                    onSelect(product);
                    setOpen(false);
                  }}
                >
                  <Check
                    className={cn(
                      "mr-2 h-4 w-4",
                      selectedItem?.id === product.id
                        ? "opacity-100"
                        : "opacity-0"
                    )}
                  />
                  <div className="flex flex-col min-w-0 flex-1">
                    <span className="truncate font-medium">
                      {product.itemName}
                    </span>
                    <div className="text-sm ">
                      <span>
                        ₹{product.unitPrice} • {product.itemUomName}
                      </span>
                      {product.gstPercentage > 0 && (
                        <span> • GST {product.gstPercentage}%</span>
                      )}
                    </div>
                  </div>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
