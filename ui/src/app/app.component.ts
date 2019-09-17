import { Component, OnInit, HostListener } from '@angular/core';
import { ScynetService } from './scynet.service';
import { NetworkState } from './model/network-state';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  public networkState: NetworkState[];

  constructor(private scynetService: ScynetService) { }

  ngOnInit()
  {
    // this.scynetService.getNetworkState()
    // .subscribe(data => {
    //   this.networkState = data;
    // });
  }

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event) {
    this.scynetService.closeHubConnection();
  }
}
