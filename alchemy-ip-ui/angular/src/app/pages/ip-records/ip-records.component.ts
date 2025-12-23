import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IpRecordService } from '../../shared/services/ip-record.service';
import { IpRecord, IpRecordDetail, PageResponse, SearchCriteria } from '../../shared/models/ip-record.model';

@Component({
  selector: 'app-ip-records',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="ip-records">
      <div class="page-header">
        <h1>IP Records</h1>
        <div class="header-actions">
          <button class="btn-secondary" (click)="toggleFilters()">
            {{ showFilters ? 'Hide Filters' : 'Show Filters' }}
          </button>
          <button class="btn-primary" (click)="loadRecords()">Refresh</button>
        </div>
      </div>

      <!-- Filters Panel -->
      <div class="filter-panel" *ngIf="showFilters">
        <div class="filter-group">
          <label>IP Address</label>
          <input type="text" [(ngModel)]="searchCriteria.ipAddress" placeholder="e.g., 192.168.1.1">
        </div>
        <div class="filter-group">
          <label>User ID</label>
          <input type="text" [(ngModel)]="searchCriteria.userId" placeholder="e.g., user@example.com">
        </div>
        <div class="filter-group">
          <label>Country Code</label>
          <input type="text" [(ngModel)]="searchCriteria.countryCode" placeholder="e.g., US" maxlength="2">
        </div>
        <div class="filter-group">
          <label>HTTP Method</label>
          <select [(ngModel)]="searchCriteria.httpMethod">
            <option value="">All</option>
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
            <option value="DELETE">DELETE</option>
            <option value="PATCH">PATCH</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Tag</label>
          <input type="text" [(ngModel)]="searchCriteria.tag" placeholder="e.g., login">
        </div>
        <div class="filter-actions">
          <button class="btn-primary" (click)="applyFilters()">Apply</button>
          <button class="btn-secondary" (click)="clearFilters()">Clear</button>
        </div>
      </div>

      <!-- Loading state -->
      <div *ngIf="loading" class="loading">
        Loading records...
      </div>

      <!-- Error state -->
      <div *ngIf="error" class="error-message">
        {{ error }}
      </div>

      <!-- Records Table -->
      <div class="table-container" *ngIf="!loading && !error">
        <table class="data-table">
          <thead>
            <tr>
              <th (click)="sortBy('id')">ID <span *ngIf="sortField === 'id'">{{ sortDirection === 'ASC' ? '▲' : '▼' }}</span></th>
              <th (click)="sortBy('ipAddress')">IP Address <span *ngIf="sortField === 'ipAddress'">{{ sortDirection === 'ASC' ? '▲' : '▼' }}</span></th>
              <th (click)="sortBy('userId')">User ID <span *ngIf="sortField === 'userId'">{{ sortDirection === 'ASC' ? '▲' : '▼' }}</span></th>
              <th>Method</th>
              <th>Path</th>
              <th>Tag</th>
              <th (click)="sortBy('countryCode')">Country <span *ngIf="sortField === 'countryCode'">{{ sortDirection === 'ASC' ? '▲' : '▼' }}</span></th>
              <th (click)="sortBy('createdAt')">Created <span *ngIf="sortField === 'createdAt'">{{ sortDirection === 'ASC' ? '▲' : '▼' }}</span></th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let record of records">
              <td>{{ record.id }}</td>
              <td class="ip-cell">{{ record.ipAddress }}</td>
              <td>{{ record.userId || '-' }}</td>
              <td><span class="badge badge-info" *ngIf="record.httpMethod">{{ record.httpMethod }}</span><span *ngIf="!record.httpMethod">-</span></td>
              <td class="path-cell" [title]="record.requestPath">{{ record.requestPath || '-' }}</td>
              <td><span class="badge badge-success" *ngIf="record.tag">{{ record.tag }}</span><span *ngIf="!record.tag">-</span></td>
              <td>{{ record.countryCode || '-' }}</td>
              <td>{{ record.createdAt | date:'short' }}</td>
              <td>
                <button class="btn-small" (click)="viewDetails(record)">View</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div *ngIf="records.length === 0" class="empty-state">
          No records found
        </div>

        <!-- Pagination -->
        <div class="pagination" *ngIf="totalElements > 0">
          <span class="pagination-info">
            Showing {{ (currentPage * pageSize) + 1 }} - {{ Math.min((currentPage + 1) * pageSize, totalElements) }} of {{ totalElements }}
          </span>
          <div class="pagination-controls">
            <button [disabled]="currentPage === 0" (click)="goToPage(0)">First</button>
            <button [disabled]="currentPage === 0" (click)="goToPage(currentPage - 1)">Previous</button>
            <span class="page-number">Page {{ currentPage + 1 }} of {{ totalPages }}</span>
            <button [disabled]="currentPage >= totalPages - 1" (click)="goToPage(currentPage + 1)">Next</button>
            <button [disabled]="currentPage >= totalPages - 1" (click)="goToPage(totalPages - 1)">Last</button>
          </div>
        </div>
      </div>

      <!-- Detail Modal -->
      <div class="modal-overlay" *ngIf="selectedRecord" (click)="closeDetails()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Details</h2>
            <button class="close-btn" (click)="closeDetails()">&times;</button>
          </div>
          <div class="modal-body" *ngIf="recordDetail">
            <div class="detail-row">
              <label>ID:</label>
              <span>{{ recordDetail.id }}</span>
            </div>
            <div class="detail-row">
              <label>IP Address:</label>
              <span class="ip-value">{{ recordDetail.ipAddress }}</span>
            </div>
            <div class="detail-row">
              <label>User ID:</label>
              <span>{{ recordDetail.userId || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>HTTP Method:</label>
              <span>{{ recordDetail.httpMethod || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>Request Path:</label>
              <span class="path-value">{{ recordDetail.requestPath || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>User Agent:</label>
              <span class="ua-value">{{ recordDetail.userAgent || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>Tag:</label>
              <span>{{ recordDetail.tag || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>Country:</label>
              <span>{{ recordDetail.countryCode || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>City:</label>
              <span>{{ recordDetail.city || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>Region:</label>
              <span>{{ recordDetail.region || 'N/A' }}</span>
            </div>
            <div class="detail-row" *ngIf="recordDetail.latitude && recordDetail.longitude">
              <label>Coordinates:</label>
              <span>{{ recordDetail.latitude }}, {{ recordDetail.longitude }}</span>
            </div>
            <div class="detail-row">
              <label>Source Header:</label>
              <span>{{ recordDetail.sourceHeader || 'N/A' }}</span>
            </div>
            <div class="detail-row">
              <label>Created At:</label>
              <span>{{ recordDetail.createdAt | date:'medium' }}</span>
            </div>
            <div class="detail-row" *ngIf="recordDetail.metadata">
              <label>Metadata:</label>
              <pre class="metadata">{{ recordDetail.metadata }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .ip-records {
      padding: 0;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }

    .filter-panel {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      align-items: flex-end;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .filter-group label {
      font-size: 12px;
      color: #8f9bb3;
    }

    .filter-group input,
    .filter-group select {
      padding: 8px 12px;
      border: 1px solid #e4e9f2;
      border-radius: 4px;
      font-size: 14px;
      min-width: 150px;
    }

    .filter-actions {
      display: flex;
      gap: 8px;
      align-items: flex-end;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .data-table th {
      cursor: pointer;
      user-select: none;
    }

    .data-table th:hover {
      background: #e4e9f2;
    }

    .ip-cell {
      font-family: monospace;
    }

    .path-cell {
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .btn-small {
      padding: 4px 8px;
      font-size: 12px;
      background: #edf1f7;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-small:hover {
      background: #e4e9f2;
    }

    .loading, .error-message, .empty-state {
      text-align: center;
      padding: 40px;
    }

    .error-message {
      background: #ffecef;
      color: #ff3d71;
      border-radius: 8px;
    }

    .empty-state {
      color: #8f9bb3;
    }

    .pagination {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-top: 1px solid #edf1f7;
    }

    .pagination-info {
      color: #8f9bb3;
      font-size: 14px;
    }

    .pagination-controls {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .pagination-controls button {
      padding: 6px 12px;
      border: 1px solid #e4e9f2;
      background: white;
      border-radius: 4px;
      cursor: pointer;
    }

    .pagination-controls button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .pagination-controls button:hover:not(:disabled) {
      background: #f7f9fc;
    }

    .page-number {
      padding: 0 12px;
      color: #8f9bb3;
    }

    /* Modal styles */
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 8px;
      width: 90%;
      max-width: 600px;
      max-height: 80vh;
      overflow-y: auto;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #edf1f7;
    }

    .modal-header h2 {
      margin: 0;
      font-size: 18px;
    }

    .close-btn {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
      color: #8f9bb3;
    }

    .modal-body {
      padding: 20px;
    }

    .detail-row {
      display: flex;
      padding: 8px 0;
      border-bottom: 1px solid #edf1f7;
    }

    .detail-row:last-child {
      border-bottom: none;
    }

    .detail-row label {
      width: 120px;
      font-weight: 500;
      color: #8f9bb3;
      flex-shrink: 0;
    }

    .detail-row span {
      flex: 1;
      word-break: break-all;
    }

    .ip-value {
      font-family: monospace;
      background: #f7f9fc;
      padding: 2px 6px;
      border-radius: 4px;
    }

    .path-value, .ua-value {
      font-size: 13px;
      color: #555;
    }

    .metadata {
      background: #f7f9fc;
      padding: 10px;
      border-radius: 4px;
      font-size: 12px;
      overflow-x: auto;
      margin: 0;
    }
  `]
})
export class IpRecordsComponent implements OnInit {
  records: IpRecord[] = [];
  loading = true;
  error: string | null = null;
  showFilters = false;

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  totalPages = 0;

  // Sorting
  sortField = 'createdAt';
  sortDirection: 'ASC' | 'DESC' = 'DESC';

  // Search criteria
  searchCriteria: SearchCriteria = {};

  // Detail view
  selectedRecord: IpRecord | null = null;
  recordDetail: IpRecordDetail | null = null;

  Math = Math;

  constructor(private ipRecordService: IpRecordService) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;
    this.error = null;

    const hasFilters = Object.values(this.searchCriteria).some(v => v && v !== '');

    const request = hasFilters
      ? this.ipRecordService.searchRecords(this.searchCriteria, this.currentPage, this.pageSize, this.sortField, this.sortDirection)
      : this.ipRecordService.getRecords(this.currentPage, this.pageSize, this.sortField, this.sortDirection);

    request.subscribe({
      next: (response) => {
        this.records = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load records. Please check if the API is running.';
        this.loading = false;
        console.error('Error loading records:', err);
      }
    });
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadRecords();
  }

  clearFilters(): void {
    this.searchCriteria = {};
    this.currentPage = 0;
    this.loadRecords();
  }

  sortBy(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = field;
      this.sortDirection = 'DESC';
    }
    this.loadRecords();
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadRecords();
  }

  viewDetails(record: IpRecord): void {
    this.selectedRecord = record;
    this.ipRecordService.getRecordById(record.id).subscribe({
      next: (detail) => {
        this.recordDetail = detail;
      },
      error: (err) => {
        console.error('Error loading record details:', err);
      }
    });
  }

  closeDetails(): void {
    this.selectedRecord = null;
    this.recordDetail = null;
  }
}
