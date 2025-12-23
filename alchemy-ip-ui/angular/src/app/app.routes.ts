import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'ip-records',
    loadComponent: () => import('./pages/ip-records/ip-records.component').then(m => m.IpRecordsComponent)
  },
  {
    path: 'statistics',
    loadComponent: () => import('./pages/statistics/statistics.component').then(m => m.StatisticsComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
