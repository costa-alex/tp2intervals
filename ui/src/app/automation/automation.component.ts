import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { DatePipe } from '@angular/common';
import { finalize, forkJoin } from 'rxjs';
import {
  ScheduledSync,
  SyncExecution,
  WorkoutClient
} from 'infrastructure/client/workout.client';
import { Platform } from 'infrastructure/platform';
import { NotificationService } from 'infrastructure/notification.service';
import { TrainingTypes } from 'infrastructure/training-types';

@Component({
  selector: 'app-automation',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule
  ],
  templateUrl: './automation.component.html',
  styleUrl: './automation.component.scss'
})
export class AutomationComponent implements OnInit {

  readonly Platform = Platform;

  readonly directions = [
    {
      title: 'TrainerRoad → TrainingPeaks',
      value: Platform.DIRECTION_TR_TP
    },
    {
      title: 'TrainerRoad → Intervals.icu',
      value: Platform.DIRECTION_TR_INT
    },
    {
      title: 'TrainingPeaks → Intervals.icu',
      value: Platform.DIRECTION_TP_INT
    },
    {
      title: 'Intervals.icu → TrainingPeaks',
      value: Platform.DIRECTION_INT_TP
    }
  ];

  readonly trainingTypes = [
    { title: 'Ride', value: 'BIKE' },
    { title: 'Virtual Ride', value: 'VIRTUAL_BIKE' },
    { title: 'MTB', value: 'MTB' },
    { title: 'Run', value: 'RUN' },
    { title: 'Swim', value: 'SWIM' },
    { title: 'Walk', value: 'WALK' },
    { title: 'Weight Training', value: 'WEIGHT' },
    { title: 'Any other', value: 'UNKNOWN' }
  ];

  formGroup: FormGroup;
  schedules: ScheduledSync[] = [];
  executions: SyncExecution[] = [];
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
    this.formGroup = this.formBuilder.group({
      direction: [
        Platform.DIRECTION_TR_TP,
        Validators.required
      ],
      trainingTypes: [
        ['BIKE', 'VIRTUAL_BIKE'],
        Validators.required
      ],
      skipSynced: [true]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  createSchedule(): void {
    if (this.formGroup.invalid) {
      return;
    }

    const direction = this.formGroup.value.direction;
    const types = this.formGroup.value.trainingTypes;
    const skipSynced = this.formGroup.value.skipSynced;

    this.loading = true;

    this.workoutClient
      .scheduleCopyCalendarToCalendar(
        types,
        skipSynced,
        direction
      )
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(() => {
        this.notificationService.scheduledSyncCreated();
        this.loadData();
      });
  }

  runNow(schedule: ScheduledSync): void {
    this.loading = true;

    this.workoutClient
      .runScheduleRequest(schedule.id)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(response => {
        this.notificationService
          .copyCalendarToCalendarCompleted(
            response,
            Platform.getTitle(schedule.sourcePlatform),
            Platform.getTitle(schedule.targetPlatform)
          );

        this.loadData();
      });
  }

  deleteSchedule(schedule: ScheduledSync): void {
    const confirmed = window.confirm(
      `Delete scheduled sync ${this.directionTitle(schedule)}?`
    );

    if (!confirmed) {
      return;
    }

    this.loading = true;

    this.workoutClient
      .deleteScheduleRequest(schedule.id)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(() => {
        this.notificationService.scheduledSyncDeleted();
        this.loadData();
      });
  }

  directionTitle(schedule: ScheduledSync): string {
    return `${Platform.getTitle(schedule.sourcePlatform)} → ` +
      `${Platform.getTitle(schedule.targetPlatform)}`;
  }

  trainingTypeTitles(types: string[]): string {
    return types
      .map(type => TrainingTypes.getTitle(type))
      .join(', ');
  }

  private loadData(): void {
    this.loading = true;

    forkJoin({
      schedules: this.workoutClient.getScheduleRequests(),
      executions: this.workoutClient.getSyncExecutions()
    })
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(({ schedules, executions }) => {
        this.schedules = schedules;
        this.executions = executions;
      });
  }

  executionDirectionTitle(execution: SyncExecution): string {
    return `${Platform.getTitle(execution.sourcePlatform)} → ` +
      `${Platform.getTitle(execution.targetPlatform)}`;
  }

  triggerTitle(
    trigger: SyncExecution['triggerType']
  ): string {
    switch (trigger) {
      case 'SCHEDULED':
        return 'Scheduled';

      case 'RUN_NOW':
        return 'Run now';

      case 'MANUAL':
        return 'Manual';
    }
  }

  statusTitle(
    status: SyncExecution['status']
  ): string {
    switch (status) {
      case 'RUNNING':
        return 'Running';

      case 'SUCCESS':
        return 'Success';

      case 'NO_CHANGES':
        return 'No changes';

      case 'PARTIAL_SUCCESS':
        return 'Partial success';

      case 'FAILED':
        return 'Failed';
    }
  }
}