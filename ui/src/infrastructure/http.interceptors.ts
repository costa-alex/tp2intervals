import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import {
  NotificationService
} from 'infrastructure/notification.service';

interface ApiErrorResponse {
  platform?: string;
  message?: string;
  code?: string;
}

export const httpErrorInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const notificationService =
    inject(NotificationService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const response =
        error.error as ApiErrorResponse | undefined;

      const message =
        response?.message ??
        error.message ??
        'An unexpected error occurred';

      const errorMessage =
        response?.platform
          ? `${response.platform}: ${message}`
          : message;

      notificationService.error(errorMessage);

      return throwError(() => error);
    })
  );
};