import { Component, OnInit, Input, OnDestroy  } from '@angular/core';
import * as d3 from 'd3';
import * as techan from 'techan';
import { ScynetService } from '../scynet.service';
import { HubConnection } from '@aspnet/signalr'

@Component({
  selector: 'app-candlestick-chart',
  templateUrl: './candlestick-chart.component.html',
  styleUrls: ['./candlestick-chart.component.css']
})
export class CandlestickChartComponent implements OnInit, OnDestroy {

  public hubConnection: HubConnection;
  public customData: any[] = [];
  public predictions: any[] = []

  public svg: any;
  public candlestick: any;
  public xAxis: any;
  public yAxis: any;
  public predictionsY: any;
  public zoomableInit: any;
  public originalX: any;
  public originalY: any;
  public originalAccessor: any;
  public cnt = 0;
  public cnt2 = 0;
  public predictionsCnt = 0;
  public predictionsIndicator: any;

  public priceSubscription;
  public predictionsSubscription;

  public _agentid: string;

  get agentid(): string {
    return this._agentid;
  }

  @Input()
  set agentid(agentid: string) {
    if(!agentid || agentid == this._agentid) return;

    if(this.predictionsSubscription) {
      this.predictionsSubscription.complete;
    }

    this._agentid = agentid;
    this.subscribeForPredictions(this.agentid);
  }

  constructor(private scynetService: ScynetService) { }

  ngOnInit() {
    // this.loadCSV("assets/data.csv").then((data: any[]) => {
    //   console.log(data);
    //   this.customData = data;
    //   this.init();
    //   this.setData();
    // });

    //var uuid = '331d591b-184d-4e7c-b075-9841181c05c1';
    //var uuid = '5fe3d339-9fe7-4d7c-8a7b-02efeced130f';
    this.init();
    this.subscribeForETHPrice();
    this.subscribeForPredictions(this.agentid);
  }

  ngOnDestroy(): void {
    this.priceSubscription.subject.complete();
    this.predictionsSubscription.subject.complete();
  }

 subscribeForPredictions(uuid) {
  if(!uuid) return;

  this.scynetService.getHubConnection((hubConnection: HubConnection) => {
    this.predictionsSubscription = hubConnection.stream('GetPredictions', uuid).subscribe({
      next: (newData: any) => {
        //this.predictions = data;
        const parseDate = d3.timeParse('%m/%d/%Y %H:%M:%S %p');
        var p = newData.value > 0.5;
        var transformed = {
          date: parseDate(newData.date),
          type: newData.isTrue === null ? 'unknown' : newData.isTrue,
          open: p ? 10 : -10,
          high: 10,
          low: -10,
          close: p ? -10 : 10,
          volume: 0
        };

        this.predictions.push(transformed);
        this.predictionsCnt = this.predictionsCnt + 1;
        this.drawPredictions(this.predictions);
      },
      error: () => {},
      complete: () => {}
    });
  });
 }

  subscribeForETHPrice() {
    this.scynetService.getHubConnection((hubConnection: HubConnection) => {
      var uuid = '30bdf7fc-8c8d-4de0-b0cf-ef65fffa7844';
      this.priceSubscription = hubConnection.stream('GetPrices', uuid).subscribe({
        next: (newCandle: any) => {
          const parseDate = d3.timeParse('%m/%d/%Y %H:%M:%S %p');
          var transformed = {
            date: parseDate(newCandle.date),
            open: +newCandle.open,
            high: +newCandle.high,
            low: +newCandle.low,
            close: +newCandle.close,
            volume: +newCandle.volumeTo
          };
          this.customData.push(transformed);
          this.updateData2();

          // if(this.customData.length >= 1) {
          //   this.updateData2();
          // }
          // this.delay(1000).then(any=>{

          // });
        },
        error: () => {},
        complete: () => {}
      });
    });
  }

  async delay(ms: number) {
    await new Promise(resolve => setTimeout(()=>resolve(), ms)).then(()=>console.log("fired"));
  }

  init(): void {
    const margin = {top: 20, right: 20, bottom: 30, left: 50},
      width = 1200 - margin.left - margin.right,
      height = 500 - margin.top - margin.bottom;

    const x = techan.scale.financetime()
      .range([0, width]);

    const y = d3.scaleLinear()
      .range([height, 0]);

    const y1 = d3.scaleLinear()
      .range([height, 0]);

    this.originalX = x;
    this.originalY = y;
    this.predictionsY = y1;

    //const zoom = d3.zoom()
      //.on('zoom', this.zoomed);

    this.candlestick = techan.plot.candlestick()
      .xScale(x)
      .yScale(y);


    this.predictionsIndicator = techan.plot.candlestick()
    .xScale(x)
    .yScale(y1);

    this.xAxis = d3.axisBottom(x);

    this.yAxis = d3.axisLeft(y);

    this.svg = d3.select('app-candlestick-chart').append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    this.svg.append('rect')
      .attr('class', 'pane')
      .attr('width', width)
      .attr('height', height)
      .style('fill', 'black');

    this.svg.append('clipPath')
      .attr('id', 'clip')
      .append('rect')
      .attr('x', 0)
      .attr('y', y(1))
      .attr('width', width)
      .attr('height', y(0) - y(1));

    this.svg.append("g")
      .attr("class", "indicatorUnknown");

    this.svg.append("g")
      .attr("class", "indicatorTrue");

    this.svg.append("g")
      .attr("class", "indicatorFalse");

    this.svg.append('g')
      .attr('class', 'candlestick')
      .attr('clip-path', 'url(#clip)');

    this.svg.append('g')
      .attr('class', 'x axis')
      .attr('transform', 'translate(0,' + height + ')');

    this.svg.append('g')
      .attr('class', 'y axis')
      .append('text')
      .attr('transform', 'rotate(-90)')
      .attr('y', 6)
      .attr('dy', '.71em')
      .style('text-anchor', 'end')
      .text('Price ($)');


      //.call(zoom);

    this.appendTooltip(width, height, x);
    this.originalAccessor = this.candlestick.accessor();
  }

  appendTooltip(width, height, x)
  {
    var tooltipDiv = d3.select("body").append("div")
    .attr("class", "tooltip")
    .style("opacity", 0);
    var data =this.customData;

    this.svg.append('rect')
        .attr('class', 'overlay')
        .attr('width', width).attr('height', height)
  	      .on("mouseover", function() {
          tooltipDiv.style('opacity', 1);
          //focus.style("display", null);
          })
          .on("mouseout", function() {
          tooltipDiv.style('opacity', 0);
          //focus.style("display", "none");
          })
          .style('fill', 'none')
          .style('pointer-events', 'all')
          .on('mousemove', function(event) {
            var bisect = d3.bisector(function(d) { return d.date}).left;
            var coords = d3.mouse(this);
            var x0 = x.invert(coords[0]),
                i = bisect(data, x0, 1),
                d0 = data[i-1],
                d1 = data[i]

            if(d0) {
              var d = x0 - d0.date > d1.date - x0 ? d1 : d0;
              var text = 'Date: ' + d3.timeFormat('%m/%d/%Y %H:%M:%S')(d.date);
              ['open', 'high', 'low', 'close'].forEach(function(key) {
                  text += '<br>' + key + ': ' + d[key];
              });
              tooltipDiv.style('left', d3.event.pageX+5+'px')
                .style('top', d3.event.pageY-30+'px')
                .html(text.trim());
            }
        });
  }

  setData() {
    const accessor = this.originalAccessor;
    //this.originalAccessor = accessor;

    this.customData = this.customData.map(function(d) {
    var parseDate = d3.timeParse("%d-%b-%y");
      return {
          date: parseDate(d.Date),
          open: +d.Open,
          high: +d.High,
          low: +d.Low,
          close: +d.Close,
          volume: +d.Volume
      };
  }).sort(function(a, b) { return d3.ascending(accessor.d(a), accessor.d(b)); });

    var data = this.customData.slice(0, 1);

    this.originalX.domain(data.map(accessor.d));
    this.originalY.domain(techan.scale.plot.ohlc(data, accessor).domain());

    //this.svg.select('g.candlestick').datum(data);
    this.draw(data);

      // Associate the zoom with the scale after a domain has been applied
      // Stash initial settings to store as baseline for zooming
    this.zoomableInit = this.originalX.zoomable().clamp(false).copy();
  }

  draw(data) {
    this.svg.select('g.candlestick').datum(data);
    this.svg.select('g.candlestick').call(this.candlestick);
    // using refresh method is more efficient as it does not perform any data joins
    // Use this if underlying data is not changing
//        svg.select('g.candlestick').call(candlestick.refresh);
    this.svg.select('g.x.axis').call(this.xAxis);
    this.svg.select('g.y.axis').call(this.yAxis);
  }

  drawPredictions(predictions) {
    this.predictionsCnt = this.predictionsCnt+1;
    var newData = [];
    if(this.predictionsCnt > 120)
    {
      newData = predictions.slice(this.predictionsCnt - 120, this.predictionsCnt);

    } else {
      newData = predictions;
    }

    this.predictionsY.domain([-1000, 10]);

    //console.log(predictions);

    this.svg.selectAll("g.indicatorUnknown").datum(predictions.filter(x => x.type == 'unknown')).call(this.predictionsIndicator);
    this.svg.selectAll("g.indicatorTrue").datum(predictions.filter(x => x.type == true)).call(this.predictionsIndicator);
    this.svg.selectAll("g.indicatorFalse").datum(predictions.filter(x => x.type == false)).call(this.predictionsIndicator);
    this.svg.select('g.x.axis').call(this.xAxis);
    this.svg.select('g.y.axis').call(this.yAxis);
  }

  redraw(data) {
    this.svg.select('g.candlestick').datum(data);
    this.svg.select('g.candlestick').call(this.candlestick);
    this.svg.select('g.x.axis').call(this.xAxis);
    this.svg.select('g.y.axis').call(this.yAxis);
  }

  zoomed() {
    const rescaledY = d3.event.transform.rescaleY(this.originalY);
    this.yAxis.scale(rescaledY);
    this.candlestick.yScale(rescaledY);

    // Emulates D3 behaviour, required for financetime due to secondary zoomable scale
    this.originalX.zoomable().domain(d3.event.transform.rescaleX(this.zoomableInit).domain());

    this.draw(this.customData);
  }

  updateData3()
  {
    this.cnt2 = this.cnt2+1;
    var newData = [];
    if(this.cnt2 >= 100)
    {
      newData = this.customData.slice(this.cnt2 - 100, this.cnt2);

    } else {
      newData = this.customData.slice(0, this.cnt2);
    }

    this.originalX.domain(newData.map(this.originalAccessor.d));
    this.originalY.domain(techan.scale.plot.ohlc(newData, this.originalAccessor).domain());
    this.redraw(newData);
  }

  updateData2()
  {
    this.cnt = this.cnt+1;
    var newData = [];
    if(this.cnt > 120)
    {
      newData = this.customData.slice(this.cnt - 120, this.cnt);

    } else {
      newData = this.customData.slice(0, this.cnt);
    }

    this.originalX.domain(newData.map(this.originalAccessor.d));
    this.originalY.domain(techan.scale.plot.ohlc(newData, this.originalAccessor).domain());
    this.redraw(newData);
  }

  updateData()
  {
    var newData;
    if(this.customData.length == 100)
    {
      newData = this.customData.slice(this.cnt - 100, this.cnt);
      this.cnt = this.cnt+1;
    } else {
      newData = this.customData;
    }

    newData = newData.sort((a, b) => {
      return d3.ascending(this.originalAccessor.d(a), this.originalAccessor.d(b));
    });
    this.originalX.domain(newData.map(this.originalAccessor.d));
    this.originalY.domain(techan.scale.plot.ohlc(newData, this.originalAccessor).domain());
    this.redraw(newData);
  }

}
