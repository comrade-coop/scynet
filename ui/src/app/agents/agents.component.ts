import { Component, OnInit } from '@angular/core';
import { ScynetService } from '../scynet.service';
import { Router } from "@angular/router";

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.css']
})
export class AgentsComponent implements OnInit {
  public agents: any[] = [];

  constructor(private router: Router, private scynetService: ScynetService) { }

  ngOnInit() {
    console.log(this.agents);
    var subscription = this.scynetService.getAgents()
    .subscribe(data => {
      Object.keys(data).forEach(key => {
        console.log(key);
        this.agents = this.agents.concat(data[key]);
        this.agents = this.agents.filter(agent => agent.value.outputShapes.length == 0)
      });
    })
  }

  showPredictions(agentKey) {
    this.router.navigate(['/agents', agentKey]);
  }

}
