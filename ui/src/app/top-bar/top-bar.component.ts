import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {EnvironmentService} from "infrastructure/environment.service";
import {MatButtonModule} from "@angular/material/button";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatBadgeModule} from "@angular/material/badge";
import {forkJoin} from "rxjs";
import {GitHubClient} from "infrastructure/client/github.client";
import * as semver from "semver";
import {MatTooltipModule} from "@angular/material/tooltip";
import { MatIconModule } from "@angular/material/icon";
import { MatSidenavModule } from "@angular/material/sidenav";
import { BreakpointObserver } from '@angular/cdk/layout';
import { map, shareReplay } from 'rxjs/operators';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { ViewChild } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    MatButtonModule,
    MatToolbarModule,
    RouterLink,
    MatBadgeModule,
    MatTooltipModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatDividerModule,
    AsyncPipe
  ],
  templateUrl: './top-bar.component.html',
  styleUrl: './top-bar.component.scss'
})

export class TopBarComponent implements OnInit {
  @ViewChild(MatDrawer)
  drawer?: MatDrawer;

  appVersion: string
  updateAvailableBadgeHidden = true;
  githubLink = 'https://github.com/costa-alex/tp2intervals'

  menuButtons = [
    {
      icon: 'home',
      name: 'Home',
      url: '/home'
    },
    {
      icon: 'timeline',
      name: 'TrainingPeaks',
      url: '/training-peaks'
    },
    {
      icon: 'directions_bike',
      name: 'TrainerRoad',
      url: '/trainer-road'
    },
    {
      icon: 'settings',
      name: 'Configuration',
      url: '/config'
    }
  ];

  constructor(
    protected router: Router,
    private githubClient: GitHubClient,
    private environmentService: EnvironmentService,
    private breakpointObserver: BreakpointObserver
  ) {
  }

  readonly isMobile$ = this.breakpointObserver.observe('(max-width: 768px)')
  .pipe(
    map(result => result.matches),
    shareReplay()
  );

  ngOnInit(): void {

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.drawer?.close();
      });

    forkJoin([
      this.githubClient.getLatestRelease(),
      this.environmentService.getVersion()
    ]).subscribe(result => {

      this.appVersion = result[1];
      const latestRelease = result[0];

      if (semver.gt(latestRelease.version, this.appVersion)) {
        this.updateAvailableBadgeHidden = false;
        this.githubLink = latestRelease.url;
      }
      console.log(result);
    });

  }
}

