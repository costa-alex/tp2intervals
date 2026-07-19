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

interface ApiErrorItem {
  platform?: string;
  message: string;
  code?: string;
}

interface ApiErrorResponse {
  platform?: string;
  message?: string;
  code?: string;
  errors?: ApiErrorItem[];
}

export const httpErrorInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const notificationService =
    inject(NotificationService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const message =
        buildErrorMessage(error);

      notificationService.error(message);

      return throwError(() => error);
    })
  );
};

function buildErrorMessage(
  error: HttpErrorResponse
): string {
  const response =
    error.error as ApiErrorResponse | undefined;

  if (response?.errors?.length) {
    return response.errors
      .map(item => {
        return item.platform
          ? `${item.platform}: ${item.message}`
          : item.message;
      })
      .join('\n');
  }

  const message = response?.message ?? error.message ?? 'An unexpected error occurred';

  return response?.platform ? `${response.platform}: ${message}` : message;
}