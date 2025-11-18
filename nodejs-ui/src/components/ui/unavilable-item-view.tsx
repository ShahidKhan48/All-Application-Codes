const UnavailableItemQtyView = ({
  unavailableItems,
}: {
  unavailableItems: any[];
}) => {
  return (
    <div className="flex flex-col gap-2">
      <div className="text-xs font-semibold text-danger">
        The following item(s) are out of stock. Kindly check with the Operations
        team.
      </div>
      <table className="w-full border p-2 text-xs">
        <thead>
          <tr>
            <th className="border p-2">Item Name</th>
            {/* <th className="w-[80px] border p-2">Reserved Qty</th> */}
            <th className="w-[80px] border p-2">Available Qty</th>
          </tr>
        </thead>
        <tbody>
          {unavailableItems?.map((item) => (
            <tr className="border-collapse" key={item.product?.id}>
              <td className="break-words border p-2">
                {item.product.descriptor?.name}
              </td>
              {/* <td className="border p-2 text-center">{item?.reservedQty}</td> */}
              <td className="border p-2 text-center">{item?.availableQty}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UnavailableItemQtyView;
