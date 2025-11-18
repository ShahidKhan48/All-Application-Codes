const Layout = ({
  headerContent,
  footerContent,
  children,
}: {
  headerContent?: JSX.Element;
  footerContent?: JSX.Element;
  children: any;
}) => {
  return (
    <div className="h-full bg-background flex flex-col gap-4">
      {headerContent && (
        <div className="bg-gradient-primary text-white p-4 shadow-lg">
          {headerContent}
        </div>
      )}
      <div className="p-4 space-y-6 flex-1 overflow-auto">{children}</div>
      {footerContent && <div className="p-2"> {footerContent}</div>}
    </div>
  );
};
export default Layout;
