import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

import { ConfigData } from '../config-data';

interface ConfigurationResponse {
  config: Record<string, string | null>;
}

interface PlatformInfoResponse {
  infoMap?: PlatformConnectionInfo;
}

type PlatformInfoMapResponse =
  Record<string, PlatformInfoResponse>;

export interface PlatformConnectionInfo {
  isValid: boolean;
  isAthlete?: boolean;
  isPremium?: boolean;
}

export interface PlatformConnectionMap {
  [platformKey: string]:
    PlatformConnectionInfo | undefined;
}

@Injectable({
  providedIn: 'root'
})
export class ConfigurationClient {

  constructor(
    private readonly httpClient: HttpClient
  ) {
  }

  getConfig(): Observable<ConfigData> {
    return this.httpClient
      .get<ConfigurationResponse>(
        '/api/configuration'
      )
      .pipe(
        map(response =>
          new ConfigData(response.config)
        )
      );
  }

  updateConfig(
    configData: ConfigData
  ): Observable<void> {
    return this.httpClient.put<void>(
      '/api/configuration',
      configData
    );
  }

  getAllPlatformInfo():
    Observable<PlatformConnectionMap> {
    return this.httpClient
      .get<PlatformInfoMapResponse>(
        '/api/configuration/platform'
      )
      .pipe(
        map(response =>
          this.unwrapPlatformInfo(response)
        )
      );
  }

  refreshAllPlatformInfo():
    Observable<PlatformConnectionMap> {
    return this.httpClient
      .post<PlatformInfoMapResponse>(
        '/api/configuration/platform/refresh',
        {}
      )
      .pipe(
        map(response =>
          this.unwrapPlatformInfo(response)
        )
      );
  }

  platformInfo(
    platform: string
  ): Observable<PlatformConnectionInfo> {
    return this.httpClient
      .get<PlatformInfoResponse>(
        `/api/configuration/${platform}`
      )
      .pipe(
        map(response =>
          response.infoMap ?? {
            isValid: false
          }
        )
      );
  }

  private unwrapPlatformInfo(
    response: PlatformInfoMapResponse
  ): PlatformConnectionMap {
    const result: PlatformConnectionMap = {};

    Object.keys(response).forEach(key => {
      result[key] =
        response[key]?.infoMap ?? {
          isValid: false
        };
    });

    return result;
  }
}