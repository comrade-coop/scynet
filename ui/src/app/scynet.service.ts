import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { NetworkState } from './model/network-state';
import { NetworkStateMock } from './mock-data/network-state-mock';
import { Signal } from './model/signal';
import { LatestSignalMock } from './mock-data/signal-mock';
import { Tournament } from './model/tournament';
import { LatestTournament } from './mock-data/tournament-mock';

import {environment} from '../environments/environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import { HubConnection, HubConnectionBuilder } from '@aspnet/signalr'

@Injectable({
  providedIn: 'root'
})
export class ScynetService {
  private _hubConnection: HubConnection;
  private _hubConnectionPromise: Promise<HubConnection>;

  constructor(private http: HttpClient) {
    var url = environment.backend + '/notify';
    this._hubConnection = new HubConnectionBuilder()
      .withUrl(url)
      .build();

    console.log(url);

    this._hubConnectionPromise = this._hubConnection
      .start()
      .then(() => {
        console.log('Connection started!');
        return this._hubConnection;
      })
      .catch(err => {
        console.log('Error while establishing connection :(');
        return this._hubConnection;
      });
   }

  getHubConnection(callback: (HubConnection) => void) {
    this._hubConnectionPromise.then(callback);
  }

  getAgents() {
    const url = environment.backend + '/api/agents';
    return this.http.get<string[]>(url);
  }

  closeHubConnection() {
    this._hubConnection.stop();
  }

  getNetworkState(): Observable<NetworkState[]> {
    return of(NetworkStateMock);
  }

  getLatestSignal(): Observable<Signal> {
    return of(LatestSignalMock);
  }

  getTournaments(): Observable<Tournament> {
    return of(LatestTournament);
  }
}
