import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import {RoutingModule} from './app-routing.module';
import { AppComponent } from './app.component';
import { NodesVisualizationComponent } from './nodes-visualization/nodes-visualization.component';
import { NodeVisualizationVisComponent } from './node-visualization-vis/node-visualization-vis.component';
import { SignalrTestComponent } from './signalr-test/signalr-test.component';
import { CandlestickChartComponent } from './candlestick-chart/candlestick-chart.component';
import { AgentsComponent } from './agents/agents.component';

@NgModule({
  declarations: [
    AppComponent,
    NodesVisualizationComponent,
    NodeVisualizationVisComponent,
    SignalrTestComponent,
    CandlestickChartComponent,
    AgentsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    RoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
