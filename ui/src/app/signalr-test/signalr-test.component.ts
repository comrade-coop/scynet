import { Component, OnInit, OnDestroy} from '@angular/core';
import { HubConnection } from '@aspnet/signalr'
import { ScynetService } from '../scynet.service';
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-signalr-test',
  templateUrl: './signalr-test.component.html',
  styleUrls: ['./signalr-test.component.css']
})
export class SignalrTestComponent implements OnInit, OnDestroy {

  public msgs: string[] = ["test1"];
  public agents: any[] = [];
  public customData: any[] = [];
  public started: boolean = false;
  public prediction: any = null;
  public predictionsSubscription;

  public agentid: string;

  constructor(private route: ActivatedRoute, private scynetService: ScynetService) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      if(this.predictionsSubscription) {
        this.predictionsSubscription.subject.complete();
      }
      var uuid = params.get("id")
      this.agentid = uuid;
      //var uuid = '331d591b-184d-4e7c-b075-9841181c05c1'
      //this.subscribeForPredictions(uuid);
    })
  }

  ngOnDestroy(): void {
    this.predictionsSubscription.subject.complete();
  }

  subscribeForPredictions(uuid) {
    this.scynetService.getHubConnection((hubConnection: HubConnection) => {
      this.predictionsSubscription = hubConnection.stream('GetPredictions', uuid).subscribe({
        next: (data: any) => {
          this.prediction = data;
        },
        error: () => {},
        complete: () => {}
      });
    });
  }
}
