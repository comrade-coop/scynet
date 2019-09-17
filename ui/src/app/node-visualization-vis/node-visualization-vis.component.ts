import { Component, OnInit, Input } from '@angular/core';
import { NetworkState } from '../model/network-state';
import { Agent } from '../model/agent';

declare var vis: any;

@Component({
  selector: 'app-node-visualization-vis',
  templateUrl: './node-visualization-vis.component.html',
  styleUrls: ['./node-visualization-vis.component.css']
})
export class NodeVisualizationVisComponent implements OnInit {

  @Input() networkstate: NetworkState[];
  nodes: any;
  edges: any;
  state: NetworkState[];
  nodeEdge: any;
  maxEdgeCnt: number;


  constructor() { }

  ngOnInit() {
    this.state = this.networkstate;
    var nodes1 = []
    var edges1 = []
    var edgeCnt = 1;
    for (let state of this.networkstate) {
      var node = {id: state.address, label: state.address, color: "red", type: "node"}
      nodes1.push(node);

      for(let agent of state.published_data) {
        var childNode = {id: agent.name, label: agent.name, type: "agent"}
        var edge = {id: edgeCnt, from: node.id, to: childNode.id, color: {color: "black"}}
        edgeCnt = edgeCnt + 1

        nodes1.push(childNode);
        edges1.push(edge);
      }
    }

    for(let i = 0; i < this.networkstate.length - 1; i++) {
      for(let j = i + 1; j < this.networkstate.length; j++) {
        var edg = {id: edgeCnt, from: this.networkstate[i].address, to: this.networkstate[j].address, type: "node"};
        edges1.push(edg);
        edgeCnt = edgeCnt + 1
      }
    }
    this.maxEdgeCnt = edgeCnt;
    this.nodeEdge = edges1[edges1.length - 1];

    this.nodes = new vis.DataSet(nodes1);
    this.edges = new vis.DataSet(edges1);

    // create a network
    var container = document.getElementById('mynetwork');

    // provide the data in the vis format
    var data = {
      nodes: this.nodes,
      edges: this.edges
    };
    var options = {};

    // initialize your network!
    var network = new vis.Network(container, data, options);
  }

  addAgent() {
    var name = Math.random().toString(36).substring(7);
    var childNode = {id: name, label: name, type: "node"}
    var edge = {from: "ABC", to: childNode.id, color: {color: "black"}}

    this.nodes.add(childNode);
    this.edges.add(edge);
  }

  addNode() {
    var name = Math.random().toString(36).substring(7);
    var newNode = {id: name, label: name, color: "red"}
  
    this.edges.remove({id: this.nodeEdge.id});

    this.nodes.add(newNode);

    var newEdge1 = {id: this.maxEdgeCnt, from: this.nodeEdge.from, to: newNode.id, color: {color: "red"}}
    this.maxEdgeCnt = this.maxEdgeCnt + 1;
    var newEdge2 = {id: this.maxEdgeCnt, from: this.nodeEdge.to, to: newNode.id, color: {color: "red"}}
    this.maxEdgeCnt = this.maxEdgeCnt + 1;
    
    this.edges.update(newEdge1);
    this.edges.update(newEdge2);
    this.nodeEdge = newEdge1;

    /*this.edges.map(edge => {
      if(edge.type == "node") 
      {
        var newEdge = {from: edge.from, to: newNode.id, color: {color: "red"}}
        this.edges.update(newEdge);

      }
    });*/
  }

}
