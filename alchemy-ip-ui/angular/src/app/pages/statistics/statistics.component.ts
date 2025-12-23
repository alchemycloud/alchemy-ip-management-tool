import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IpRecordService } from '../../shared/services/ip-record.service';
import { GeoDistribution, FrequentIp, PageResponse } from '../../shared/models/ip-record.model';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="statistics">
      <div class="page-header">
        <h1>Statistics</h1>
        <button class="btn-primary" (click)="refresh()">Refresh</button>
      </div>

      <!-- Loading state -->
      <div *ngIf="loading" class="loading">
        Loading statistics...
      </div>

      <!-- Error state -->
      <div *ngIf="error" class="error-message">
        {{ error }}
      </div>

      <div class="stats-content" *ngIf="!loading && !error">
        <!-- Geographic Distribution -->
        <div class="stats-section">
          <h2>Geographic Distribution</h2>
          <div class="geo-grid">
            <!-- Countries -->
            <div class="chart-container">
              <h3>By Country</h3>
              <div class="distribution-list">
                <div class="dist-item" *ngFor="let country of countryList">
                  <span class="dist-label">{{ country.code }}</span>
                  <div class="dist-bar-container">
                    <div class="dist-bar" [style.width.%]="getPercentage(country.count, maxCountryCount)"></div>
                  </div>
                  <span class="dist-count">{{ country.count }}</span>
                </div>
                <div *ngIf="countryList.length === 0" class="empty-state">
                  No geographic data available
                </div>
              </div>
            </div>

            <!-- Cities -->
            <div class="chart-container">
              <h3>By City (Top 20)</h3>
              <div class="distribution-list">
                <div class="dist-item" *ngFor="let city of cityList">
                  <span class="dist-label">{{ city.name }}</span>
                  <div class="dist-bar-container">
                    <div class="dist-bar city-bar" [style.width.%]="getPercentage(city.count, maxCityCount)"></div>
                  </div>
                  <span class="dist-count">{{ city.count }}</span>
                </div>
                <div *ngIf="cityList.length === 0" class="empty-state">
                  No city data available
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Frequent IPs -->
        <div class="stats-section">
          <h2>Frequent IP Addresses</h2>
          <div class="filter-row">
            <label>Minimum request count:</label>
            <input type="number" [(ngModel)]="threshold" min="1" (change)="loadFrequentIps()">
          </div>
          <div class="chart-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Rank</th>
                  <th>IP Address</th>
                  <th>Request Count</th>
                  <th>% of Total</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let ip of frequentIps; let i = index">
                  <td><span class="rank-badge">{{ i + 1 }}</span></td>
                  <td class="ip-cell">{{ ip.ipAddress }}</td>
                  <td>{{ ip.count | number }}</td>
                  <td>
                    <div class="percentage-bar">
                      <div class="percentage-fill" [style.width.%]="getPercentage(ip.count, totalRequestsFromFrequent)"></div>
                      <span>{{ getPercentage(ip.count, totalRequestsFromFrequent) | number:'1.1-1' }}%</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
            <div *ngIf="frequentIps.length === 0" class="empty-state">
              No IP addresses meet the threshold
            </div>

            <!-- Pagination for frequent IPs -->
            <div class="pagination" *ngIf="frequentIpsTotalPages > 1">
              <button [disabled]="frequentIpsPage === 0" (click)="loadFrequentIps(frequentIpsPage - 1)">Previous</button>
              <span>Page {{ frequentIpsPage + 1 }} of {{ frequentIpsTotalPages }}</span>
              <button [disabled]="frequentIpsPage >= frequentIpsTotalPages - 1" (click)="loadFrequentIps(frequentIpsPage + 1)">Next</button>
            </div>
          </div>
        </div>

        <!-- Lookup Tools -->
        <div class="stats-section">
          <h2>Lookup Tools</h2>
          <div class="lookup-grid">
            <!-- IP Lookup -->
            <div class="chart-container">
              <h3>IP Address Lookup</h3>
              <div class="lookup-form">
                <input type="text" [(ngModel)]="lookupIp" placeholder="Enter IP address">
                <button class="btn-primary" (click)="lookupIpAddress()">Lookup</button>
              </div>
              <div class="lookup-result" *ngIf="ipLookupResult">
                <div class="result-row">
                  <label>Total Requests:</label>
                  <span>{{ ipLookupResult.count | number }}</span>
                </div>
                <div class="result-row">
                  <label>Distinct Users:</label>
                  <span>{{ ipDistinctUsers }}</span>
                </div>
              </div>
            </div>

            <!-- User Lookup -->
            <div class="chart-container">
              <h3>User Lookup</h3>
              <div class="lookup-form">
                <input type="text" [(ngModel)]="lookupUser" placeholder="Enter user ID">
                <button class="btn-primary" (click)="lookupUserAddress()">Lookup</button>
              </div>
              <div class="lookup-result" *ngIf="userLookupResult">
                <div class="result-row">
                  <label>Total Requests:</label>
                  <span>{{ userLookupResult.count | number }}</span>
                </div>
                <div class="result-row">
                  <label>Distinct IPs:</label>
                  <span>{{ userDistinctIps }}</span>
                </div>
                <div class="result-row" *ngIf="userIpAddresses.length > 0">
                  <label>IP Addresses:</label>
                  <div class="ip-list">
                    <span class="ip-tag" *ngFor="let ip of userIpAddresses">{{ ip }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .statistics {
      padding: 0;
    }

    .loading, .error-message {
      text-align: center;
      padding: 40px;
    }

    .error-message {
      background: #ffecef;
      color: #ff3d71;
      border-radius: 8px;
    }

    .stats-section {
      margin-bottom: 32px;
    }

    .stats-section h2 {
      font-size: 18px;
      margin-bottom: 16px;
      color: #222b45;
    }

    .geo-grid, .lookup-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
    }

    .distribution-list {
      max-height: 400px;
      overflow-y: auto;
    }

    .dist-item {
      display: flex;
      align-items: center;
      padding: 8px 0;
      gap: 12px;
    }

    .dist-label {
      width: 60px;
      font-weight: 500;
      flex-shrink: 0;
    }

    .dist-bar-container {
      flex: 1;
      height: 20px;
      background: #edf1f7;
      border-radius: 4px;
      overflow: hidden;
    }

    .dist-bar {
      height: 100%;
      background: #3366ff;
      border-radius: 4px;
      transition: width 0.3s ease;
    }

    .dist-bar.city-bar {
      background: #00d68f;
    }

    .dist-count {
      width: 50px;
      text-align: right;
      color: #8f9bb3;
      font-size: 13px;
    }

    .filter-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
    }

    .filter-row label {
      color: #8f9bb3;
    }

    .filter-row input {
      width: 80px;
      padding: 8px;
      border: 1px solid #e4e9f2;
      border-radius: 4px;
    }

    .rank-badge {
      display: inline-block;
      width: 24px;
      height: 24px;
      background: #3366ff;
      color: white;
      border-radius: 50%;
      text-align: center;
      line-height: 24px;
      font-size: 12px;
    }

    .ip-cell {
      font-family: monospace;
    }

    .percentage-bar {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .percentage-fill {
      height: 8px;
      background: #00d68f;
      border-radius: 4px;
      min-width: 4px;
    }

    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16px;
      padding: 16px;
    }

    .pagination button {
      padding: 6px 12px;
      border: 1px solid #e4e9f2;
      background: white;
      border-radius: 4px;
      cursor: pointer;
    }

    .pagination button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .lookup-form {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
    }

    .lookup-form input {
      flex: 1;
      padding: 8px 12px;
      border: 1px solid #e4e9f2;
      border-radius: 4px;
    }

    .lookup-result {
      background: #f7f9fc;
      padding: 16px;
      border-radius: 8px;
    }

    .result-row {
      display: flex;
      padding: 8px 0;
      border-bottom: 1px solid #edf1f7;
    }

    .result-row:last-child {
      border-bottom: none;
    }

    .result-row label {
      width: 120px;
      font-weight: 500;
      color: #8f9bb3;
    }

    .ip-list {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }

    .ip-tag {
      background: #edf1f7;
      padding: 2px 8px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 12px;
    }

    .empty-state {
      text-align: center;
      padding: 40px;
      color: #8f9bb3;
    }

    @media (max-width: 768px) {
      .geo-grid, .lookup-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class StatisticsComponent implements OnInit {
  loading = true;
  error: string | null = null;

  // Geographic data
  geoDistribution: GeoDistribution | null = null;
  countryList: { code: string; count: number }[] = [];
  cityList: { name: string; count: number }[] = [];
  maxCountryCount = 0;
  maxCityCount = 0;

  // Frequent IPs
  frequentIps: FrequentIp[] = [];
  threshold = 2;
  frequentIpsPage = 0;
  frequentIpsTotalPages = 0;
  totalRequestsFromFrequent = 0;

  // Lookup tools
  lookupIp = '';
  lookupUser = '';
  ipLookupResult: { count: number } | null = null;
  ipDistinctUsers = 0;
  userLookupResult: { count: number } | null = null;
  userDistinctIps = 0;
  userIpAddresses: string[] = [];

  constructor(private ipRecordService: IpRecordService) {}

  ngOnInit(): void {
    this.loadData();
  }

  refresh(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    // Load geographic distribution
    this.ipRecordService.getGeoDistribution().subscribe({
      next: (geo) => {
        this.geoDistribution = geo;
        this.countryList = Object.entries(geo.countryDistribution || {})
          .map(([code, count]) => ({ code, count }))
          .sort((a, b) => b.count - a.count);
        this.maxCountryCount = Math.max(...this.countryList.map(c => c.count), 1);

        this.cityList = Object.entries(geo.cityDistribution || {})
          .map(([name, count]) => ({ name, count }))
          .sort((a, b) => b.count - a.count);
        this.maxCityCount = Math.max(...this.cityList.map(c => c.count), 1);

        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load statistics. Please check if the API is running.';
        this.loading = false;
        console.error('Error loading geo distribution:', err);
      }
    });

    this.loadFrequentIps();
  }

  loadFrequentIps(page: number = 0): void {
    this.frequentIpsPage = page;
    this.ipRecordService.getFrequentIps(this.threshold, page, 20).subscribe({
      next: (response) => {
        this.frequentIps = response.content;
        this.frequentIpsTotalPages = response.totalPages;
        this.totalRequestsFromFrequent = this.frequentIps.reduce((sum, ip) => sum + ip.count, 0);
      },
      error: (err) => {
        console.error('Error loading frequent IPs:', err);
      }
    });
  }

  getPercentage(value: number, total: number): number {
    if (total === 0) return 0;
    return (value / total) * 100;
  }

  lookupIpAddress(): void {
    if (!this.lookupIp.trim()) return;

    this.ipLookupResult = null;
    this.ipDistinctUsers = 0;

    this.ipRecordService.getCountByIp(this.lookupIp).subscribe({
      next: (result) => {
        this.ipLookupResult = result;
      },
      error: (err) => {
        console.error('Error looking up IP:', err);
      }
    });

    this.ipRecordService.getDistinctUsersForIp(this.lookupIp).subscribe({
      next: (result) => {
        this.ipDistinctUsers = result.count;
      },
      error: (err) => {
        console.error('Error getting distinct users:', err);
      }
    });
  }

  lookupUserAddress(): void {
    if (!this.lookupUser.trim()) return;

    this.userLookupResult = null;
    this.userDistinctIps = 0;
    this.userIpAddresses = [];

    this.ipRecordService.getCountByUser(this.lookupUser).subscribe({
      next: (result) => {
        this.userLookupResult = result;
      },
      error: (err) => {
        console.error('Error looking up user:', err);
      }
    });

    this.ipRecordService.getDistinctIpsForUser(this.lookupUser).subscribe({
      next: (result) => {
        this.userDistinctIps = result.count;
        this.userIpAddresses = result.ipAddresses;
      },
      error: (err) => {
        console.error('Error getting distinct IPs:', err);
      }
    });
  }
}
