import {
  provideHttpClient
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import {
  TestBed
} from '@angular/core/testing';

import {
  ApplicationInfoClient
} from './application-info.client';

describe('ApplicationInfoClient', () => {
  let client: ApplicationInfoClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApplicationInfoClient,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    client = TestBed.inject(ApplicationInfoClient);

    httpTestingController =
      TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should retrieve the application version', () => {
    client.getVersion().subscribe(version => {
      expect(version).toBe('0.4.12');
    });

    const request =
      httpTestingController.expectOne('/actuator/info');

    expect(request.request.method).toBe('GET');

    request.flush({
      build: {
        version: '0.4.12'
      }
    });
  });
});