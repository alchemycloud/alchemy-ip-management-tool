import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  IpRecord,
  IpRecordDetail,
  PageResponse,
  IpStats,
  TimelineData,
  GeoDistribution,
  FrequentIp,
  SearchCriteria,
  CurrentUser
} from '../models/ip-record.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class IpRecordService {
  private baseUrl = environment.apiUrl + '/api/alchemy-ip';

  constructor(private http: HttpClient) {}

  // Records endpoints
  getRecords(page: number = 0, size: number = 20, sortBy: string = 'createdAt', direction: string = 'DESC'): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<PageResponse<IpRecord>>(`${this.baseUrl}/records`, { params });
  }

  getRecordById(id: number): Observable<IpRecordDetail> {
    return this.http.get<IpRecordDetail>(`${this.baseUrl}/records/${id}`);
  }

  getRecordsByIp(ipAddress: string, page: number = 0, size: number = 20): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<IpRecord>>(`${this.baseUrl}/records/ip/${ipAddress}`, { params });
  }

  getRecordsByUser(userId: string, page: number = 0, size: number = 20): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<IpRecord>>(`${this.baseUrl}/records/user/${userId}`, { params });
  }

  getRecordsByTag(tag: string, page: number = 0, size: number = 20): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<IpRecord>>(`${this.baseUrl}/records/tag/${tag}`, { params });
  }

  getRecordsByCountry(countryCode: string, page: number = 0, size: number = 20): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<IpRecord>>(`${this.baseUrl}/records/country/${countryCode}`, { params });
  }

  getRecordsByDateRange(startDate: string, endDate: string, page: number = 0, size: number = 20): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<IpRecord>>(`${this.baseUrl}/records/date-range`, { params });
  }

  searchRecords(criteria: SearchCriteria, page: number = 0, size: number = 20, sortBy: string = 'createdAt', direction: string = 'DESC'): Observable<PageResponse<IpRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.post<PageResponse<IpRecord>>(`${this.baseUrl}/records/search`, criteria, { params });
  }

  deleteOldRecords(timestamp: string): Observable<{ deletedCount: number }> {
    return this.http.delete<{ deletedCount: number }>(`${this.baseUrl}/records/before/${timestamp}`);
  }

  // Stats endpoints
  getSummaryStats(): Observable<IpStats> {
    return this.http.get<IpStats>(`${this.baseUrl}/stats/summary`);
  }

  getTimelineData(days: number = 30): Observable<TimelineData> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<TimelineData>(`${this.baseUrl}/stats/timeline`, { params });
  }

  getGeoDistribution(): Observable<GeoDistribution> {
    return this.http.get<GeoDistribution>(`${this.baseUrl}/stats/geographic`);
  }

  getFrequentIps(threshold: number = 2, page: number = 0, size: number = 20): Observable<PageResponse<FrequentIp>> {
    const params = new HttpParams()
      .set('threshold', threshold.toString())
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<FrequentIp>>(`${this.baseUrl}/stats/frequent`, { params });
  }

  getCountByIp(ipAddress: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/stats/ip/${ipAddress}/count`);
  }

  getCountByUser(userId: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/stats/user/${userId}/count`);
  }

  getDistinctIpsForUser(userId: string): Observable<{ count: number; ipAddresses: string[] }> {
    return this.http.get<{ count: number; ipAddresses: string[] }>(`${this.baseUrl}/stats/user/${userId}/distinct-ips`);
  }

  getDistinctUsersForIp(ipAddress: string): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/stats/ip/${ipAddress}/distinct-users`);
  }

  // Auth endpoint
  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.baseUrl}/current-user`);
  }
}
