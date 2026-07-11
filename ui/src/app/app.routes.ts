import { Routes } from '@angular/router';

import { HomeComponent } from 'app/home/home.component';
import { ConfigurationComponent } from 'app/configuration/configuration.component';
import { TrainingPeaksComponent } from 'app/training-peaks/training-peaks.component';
import { TrainerRoadComponent } from 'app/trainer-road/trainer-road.component';
import { AutomationComponent } from 'app/automation/automation.component';

export const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'training-peaks',
    component: TrainingPeaksComponent
  },
  {
    path: 'trainer-road',
    component: TrainerRoadComponent
  },
  {
    path: 'automation',
    component: AutomationComponent
  },
  {
    path: 'config',
    component: ConfigurationComponent
  },
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }
];