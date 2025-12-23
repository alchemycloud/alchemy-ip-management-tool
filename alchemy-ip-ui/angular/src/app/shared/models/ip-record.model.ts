export interface IpRecord {
  id: number;
  ipAddress: string;
  userId: string | null;
  httpMethod: string | null;
  requestPath: string | null;
  tag: string | null;
  countryCode: string | null;
  createdAt: string;
}

export interface IpRecordDetail extends IpRecord {
  userAgent: string | null;
  city: string | null;
  region: string | null;
  latitude: number | null;
  longitude: number | null;
  sourceHeader: string | null;
  metadata: string | null;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface IpStats {
  totalRecords: number;
  uniqueIpAddresses: number;
  uniqueUsers: number;
  recordsToday: number;
  recordsThisWeek: number;
  topCountries: { [key: string]: number };
  topIpAddresses: FrequentIp[];
  recentRecords: IpRecord[];
  oldestRecord: string | null;
  newestRecord: string | null;
}

export interface FrequentIp {
  ipAddress: string;
  count: number;
}

export interface TimelineData {
  labels: string[];
  counts: number[];
}

export interface GeoDistribution {
  countryDistribution: { [key: string]: number };
  cityDistribution: { [key: string]: number };
}

export interface SearchCriteria {
  ipAddress?: string;
  userId?: string;
  tag?: string;
  countryCode?: string;
  httpMethod?: string;
  requestPathPattern?: string;
  startDate?: string;
  endDate?: string;
}

export interface CurrentUser {
  authenticated: boolean;
  username?: string;
  authorities?: string[];
}
