// Mock data for the sales agent application

export interface Agent {
  id: string;
  name: string;
  phone: string;
  role: 'sales' | 'collection';
}

export interface Customer {
  id: string;
  name: string;
  address: string;
  phone: string;
  area: string;
  outstandingAmount: number;
}

export interface Product {
  id: string;
  name: string;
  category: string;
  price: number;
  unit: string;
  stock: number;
}

export interface Task {
  id: string;
  type: 'visit' | 'collection' | 'delivery' | 'onboarding';
  customerId: string;
  customer: Customer;
  date: string;
  status: 'pending' | 'in-progress' | 'completed';
  priority: 'low' | 'medium' | 'high';
  description: string;
  orderId?: string;
  amount?: number;
}

export interface Order {
  id: string;
  customerId: string;
  items: OrderItem[];
  totalAmount: number;
  deliveryDate: string;
  status: 'pending' | 'confirmed' | 'delivered';
  createdAt: string;
}

export interface OrderItem {
  productId: string;
  product: Product;
  quantity: number;
  price: number;
  total: number;
}

export interface Collection {
  method: string;
  amount: number;
  approval_status: string;
  bank_reference_no: string;
  cash_transaction_id: number;
  comments: string;
  expiry_by: string | null;
  image_url: string;
  name: string;
  payment_link: null | string;
  payment_mode: 'CASH' | 'NEFT' | 'UPI' | 'PAYMENT_LINK' | 'CHEQUE' | 'QR' ;
  qr_link: null | string;
  rejection_reason: null | string;
  status: string;
  transaction_creation_date: string;
}

// Mock current agent
export const currentAgent: Agent = {
  id: 'agent-001',
  name: 'Rajesh Kumar',
  phone: '+91 98765 43210',
  role: 'sales',
};

// Mock customers
export const mockCustomers: Customer[] = [
  {
    id: 'cust-001',
    name: 'Sharma General Store',
    address: 'Shop No. 15, Market Road, Sector 14',
    phone: '+91 98123 45678',
    area: 'Sector 14',
    outstandingAmount: 15000,
  },
  {
    id: 'cust-002',
    name: 'Patel Traders',
    address: 'Plot 22, Industrial Area, Phase 1',
    phone: '+91 97654 32109',
    area: 'Industrial Area',
    outstandingAmount: 8500,
  },
  {
    id: 'cust-003',
    name: 'Singh Electronics',
    address: 'Building A-12, Commercial Complex',
    phone: '+91 96543 21098',
    area: 'Commercial Complex',
    outstandingAmount: 22000,
  },
  {
    id: 'cust-004',
    name: 'Gupta Wholesale',
    address: 'Warehouse 8, Transport Nagar',
    phone: '+91 95432 10987',
    area: 'Transport Nagar',
    outstandingAmount: 0,
  },
];

// Mock products catalog
export const mockProducts: Product[] = [
  {
    id: 'prod-001',
    name: 'Premium Rice 25kg',
    category: 'Grains',
    price: 1200,
    unit: 'bag',
    stock: 150,
  },
  {
    id: 'prod-002',
    name: 'Cooking Oil 5L',
    category: 'Oil',
    price: 550,
    unit: 'bottle',
    stock: 80,
  },
  {
    id: 'prod-003',
    name: 'Sugar 50kg',
    category: 'Sweeteners',
    price: 2200,
    unit: 'bag',
    stock: 60,
  },
  {
    id: 'prod-004',
    name: 'Wheat Flour 25kg',
    category: 'Flour',
    price: 800,
    unit: 'bag',
    stock: 120,
  },
  {
    id: 'prod-005',
    name: 'Dal Mix 10kg',
    category: 'Pulses',
    price: 900,
    unit: 'bag',
    stock: 90,
  },
];

// Mock today's tasks
export const mockTasks: Task[] = [
  {
    id: 'task-001',
    type: 'visit',
    customerId: 'cust-001',
    customer: mockCustomers[0],
    date: new Date().toISOString().split('T')[0],
    status: 'pending',
    priority: 'high',
    description: 'Monthly inventory check and new order placement',
  },
  {
    id: 'task-002',
    type: 'collection',
    customerId: 'cust-002',
    customer: mockCustomers[1],
    date: new Date().toISOString().split('T')[0],
    status: 'pending',
    priority: 'medium',
    description: 'Collect outstanding payment of ₹8,500',
    amount: 8500,
  },
  {
    id: 'task-003',
    type: 'delivery',
    customerId: 'cust-003',
    customer: mockCustomers[2],
    date: new Date().toISOString().split('T')[0],
    status: 'in-progress',
    priority: 'high',
    description: 'Deliver order #ORD-1234',
    orderId: 'ORD-1234',
  },
  {
    id: 'task-004',
    type: 'onboarding',
    customerId: 'cust-004',
    customer: mockCustomers[3],
    date: new Date().toISOString().split('T')[0],
    status: 'pending',
    priority: 'low',
    description: 'Complete onboarding process for new customer',
  },
];

// Mock collections for today
export const mockCollections: Collection[] = [
  {
    id: 'coll-001',
    customerId: 'cust-002',
    customer: mockCustomers[1],
    amount: 5000,
    method: 'upi',
    reference: 'UPI123456789',
    date: new Date().toISOString().split('T')[0],
    time: '10:30 AM',
  },
  {
    id: 'coll-002',
    customerId: 'cust-001',
    customer: mockCustomers[0],
    amount: 3000,
    method: 'cash',
    date: new Date().toISOString().split('T')[0],
    time: '02:15 PM',
  },
];

// Helper functions
export const getTasksByStatus = (status: Task['status']) => {
  return mockTasks.filter((task) => task.status === status);
};

export const getTasksByType = (type: Task['type']) => {
  return mockTasks.filter((task) => task.type === type);
};

export const getTotalCollectionsToday = () => {
  return mockCollections.reduce(
    (total, collection) => total + collection.amount,
    0
  );
};

export const getCustomerById = (id: string) => {
  return mockCustomers.find((customer) => customer.id === id);
};

export const getProductById = (id: string) => {
  return mockProducts.find((product) => product.id === id);
};
