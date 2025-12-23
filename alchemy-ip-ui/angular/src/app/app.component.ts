import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-container">
      <nav class="sidebar">
        <div class="logo">
          <h2>IP Dashboard</h2>
        </div>
        <ul class="nav-menu">
          <li>
            <a routerLink="/dashboard" routerLinkActive="active">
              <span class="icon">&#x1F4CA;</span>
              <span>Dashboard</span>
            </a>
          </li>
          <li>
            <a routerLink="/ip-records" routerLinkActive="active">
              <span class="icon">&#x1F4CB;</span>
              <span>IP Records</span>
            </a>
          </li>
          <li>
            <a routerLink="/statistics" routerLinkActive="active">
              <span class="icon">&#x1F4C8;</span>
              <span>Statistics</span>
            </a>
          </li>
        </ul>
      </nav>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      min-height: 100vh;
    }

    .sidebar {
      width: 250px;
      background: #222b45;
      color: white;
      padding: 20px 0;
      position: fixed;
      height: 100vh;
      overflow-y: auto;
    }

    .logo {
      padding: 0 20px 20px;
      border-bottom: 1px solid #3a4467;
    }

    .logo h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
    }

    .nav-menu {
      list-style: none;
      padding: 20px 0;
      margin: 0;
    }

    .nav-menu li a {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      color: #8f9bb3;
      text-decoration: none;
      transition: all 0.2s;
    }

    .nav-menu li a:hover,
    .nav-menu li a.active {
      background: #3a4467;
      color: white;
    }

    .nav-menu li a .icon {
      margin-right: 12px;
      font-size: 18px;
    }

    .main-content {
      margin-left: 250px;
      flex: 1;
      padding: 24px;
      background: #edf1f7;
      min-height: 100vh;
    }
  `]
})
export class AppComponent {
  title = 'IP Management Dashboard';
}
