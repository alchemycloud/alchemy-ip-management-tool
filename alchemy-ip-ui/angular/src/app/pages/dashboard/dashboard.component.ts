import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IpRecordService } from '../../shared/services/ip-record.service';
import { IpStats, IpRecord, TimelineData } from '../../shared/models/ip-record.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard">
      <div class="page-header">
        <h1>Dashboard</h1>
        <button class="btn-primary" (click)="refresh()">Refresh</button>
      </div>

      <!-- Stats Cards -->
      <div class="stats-grid" *ngIf="stats">
        <div class="stats-card">
          <h3>Total Records</h3>
          <div class="value">{{ stats.totalRecords | number }}</div>
        </div>
        <div class="stats-card">
          <h3>Unique IPs</h3>
          <div class="value">{{ stats.uniqueIpAddresses | number }}</div>
        </div>
        <div class="stats-card">
          <h3>Unique Users</h3>
          <div class="value">{{ stats.uniqueUsers | number }}</div>
        </div>
        <div class="stats-card">
          <h3>Today</h3>
          <div class="value">{{ stats.recordsToday | number }}</div>
        </div>
        <div class="stats-card">
          <h3>This Week</h3>
          <div class="value">{{ stats.recordsThisWeek | number }}</div>
        </div>
      </div>

      <!-- Loading state -->
      <div *ngIf="loading" class="loading">
        Loading dashboard data...
      </div>

      <!-- Error state -->
      <div *ngIf="error" class="error-message">
        {{ error }}
      </div>

      <div class="dashboard-grid" *ngIf="stats && !loading">
        <!-- Top IPs -->
        <div class="chart-container">
          <h3>Top IP Addresses</h3>
          <div class="top-list">
            <div class="list-item" *ngFor="let ip of stats.topIpAddresses; let i = index">
              <span class="rank">{{ i + 1 }}</span>
              <span class="ip">{{ ip.ipAddress }}</span>
              <span class="count">{{ ip.count }} requests</span>
            </div>
            <div *ngIf="stats.topIpAddresses.length === 0" class="empty-state">
              No data available
            </div>
          </div>
        </div>

        <!-- Top Countries -->
        <div class="chart-container">
          <h3>Top Countries</h3>
          <div class="top-list">
            <div class="list-item" *ngFor="let country of topCountriesList; let i = index">
              <span class="rank">{{ i + 1 }}</span>
              <span class="country">{{ country.code }}</span>
              <span class="count">{{ country.count }} requests</span>
            </div>
            <div *ngIf="topCountriesList.length === 0" class="empty-state">
              No geo data available
            </div>
          </div>
        </div>

        <!-- Recent Activity -->
        <div class="chart-container full-width">
          <h3>Recent Activity</h3>
          <table class="data-table">
            <thead>
              <tr>
                <th>IP Address</th>
                <th>User</th>
                <th>Method</th>
                <th>Path</th>
                <th>Country</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let record of stats.recentRecords">
                <td>{{ record.ipAddress }}</td>
                <td>{{ record.userId || '-' }}</td>
                <td><span class="badge badge-info">{{ record.httpMethod || 'N/A' }}</span></td>
                <td class="path-cell">{{ record.requestPath || '-' }}</td>
                <td>{{ record.countryCode || '-' }}</td>
                <td>{{ record.createdAt | date:'short' }}</td>
              </tr>
            </tbody>
          </table>
          <div *ngIf="stats.recentRecords.length === 0" class="empty-state">
            No recent activity
          </div>
        </div>

        <!-- Timeline Chart (placeholder) -->
        <div class="chart-container full-width" *ngIf="timeline">
          <h3>Records Over Time (Last {{ timeline.labels.length }} Days)</h3>
          <div class="timeline-bars">
            <div class="bar-container" *ngFor="let count of timeline.counts; let i = index">
              <div class="bar" [style.height.%]="getBarHeight(count)"></div>
              <span class="label">{{ timeline.labels[i] | date:'M/d' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      padding: 0;
    }

    .loading {
      text-align: center;
      padding: 40px;
      color: #8f9bb3;
    }

    .error-message {
      background: #ffecef;
      color: #ff3d71;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .dashboard-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
    }

    .full-width {
      grid-column: span 2;
    }

    .top-list {
      max-height: 300px;
      overflow-y: auto;
    }

    .list-item {
      display: flex;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid #edf1f7;
    }

    .list-item:last-child {
      border-bottom: none;
    }

    .rank {
      width: 30px;
      height: 30px;
      background: #3366ff;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 500;
      margin-right: 12px;
    }

    .ip, .country {
      flex: 1;
      font-family: monospace;
      font-size: 14px;
    }

    .count {
      color: #8f9bb3;
      font-size: 13px;
    }

    .path-cell {
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .empty-state {
      text-align: center;
      padding: 40px;
      color: #8f9bb3;
    }

    .timeline-bars {
      display: flex;
      align-items: flex-end;
      height: 200px;
      gap: 4px;
      padding: 10px 0;
      overflow-x: auto;
    }

    .bar-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      min-width: 30px;
    }

    .bar {
      width: 20px;
      background: #3366ff;
      border-radius: 4px 4px 0 0;
      min-height: 4px;
    }

    .bar-container .label {
      font-size: 10px;
      color: #8f9bb3;
      margin-top: 4px;
      transform: rotate(-45deg);
      transform-origin: center;
    }

    @media (max-width: 768px) {
      .dashboard-grid {
        grid-template-columns: 1fr;
      }

      .full-width {
        grid-column: span 1;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  stats: IpStats | null = null;
  timeline: TimelineData | null = null;
  loading = true;
  error: string | null = null;
  topCountriesList: { code: string; count: number }[] = [];
  private maxCount = 0;

  constructor(private ipRecordService: IpRecordService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  refresh(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.error = null;

    // Load summary stats
    this.ipRecordService.getSummaryStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.topCountriesList = Object.entries(stats.topCountries || {})
          .map(([code, count]) => ({ code, count }))
          .sort((a, b) => b.count - a.count)
          .slice(0, 10);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load dashboard data. Please check if the API is running.';
        this.loading = false;
        console.error('Error loading stats:', err);
      }
    });

    // Load timeline data
    this.ipRecordService.getTimelineData(14).subscribe({
      next: (timeline) => {
        this.timeline = timeline;
        this.maxCount = Math.max(...timeline.counts, 1);
      },
      error: (err) => {
        console.error('Error loading timeline:', err);
      }
    });
  }

  getBarHeight(count: number): number {
    return (count / this.maxCount) * 100;
  }
}
