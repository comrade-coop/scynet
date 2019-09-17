import { Component, OnInit, Input } from '@angular/core';
import { NetworkState } from '../model/network-state';
//import * as Konva from 'konva';

declare var Konva: any;


@Component({
    selector: 'app-nodes-visualization',
    templateUrl: './nodes-visualization.component.html',
    styleUrls: ['./nodes-visualization.component.css']
})
export class NodesVisualizationComponent implements OnInit {

    @Input() networkstate: NetworkState[];

    stage: any;
    layer: any;

    ngOnInit() {
        var x = this.networkstate;
        this.init();
    }

    addAgent(): void {
        var rect = this.stage.findOne('#ABC');
        var group = this.stage.findOne('#group-ABC');
        var lastAgent = this.stage.findOne("#child-agent7");
        var sh1 = lastAgent.getX();
        var sh2 = lastAgent.getY();
        
        var diagonal = 50 / Math.SQRT2;
        var radius2 = diagonal + 30;
        //var angle = (3 / 4) * Math.PI;
        var angle = sh2/sh1;
        var x = radius2 * Math.cos(angle) + (50 / 2);
        var y = radius2 * Math.sin(angle) + (50 / 2);

        var childRect = new Konva.Rect({
            x: rect.attrs.x + (rect.attrs.width / 2), //find center point of blue rect
            y: rect.attrs.y + (rect.attrs.height / 2),
            width: 30,
            height: 30,
            fill: 'green',
            stroke: 'black',
            strokeWidth: 4,
            offset: {
                x: x,
                y: y + angle
            }
        });

        group.add(childRect);

        var angularSpeed = 20;
        var anim = new Konva.Animation(function (frame) {
            var angleDiff = frame.timeDiff * angularSpeed / 1000;
            childRect.rotate(angleDiff);
            
        }, this.layer);

        anim.start()
    }

    init(): void {
        var width = window.innerWidth - 100;
        var height = window.innerHeight - 100;

        this.stage = new Konva.Stage({
            container: 'container',
            width: width,
            height: height
        });

        this.layer = new Konva.Layer();

        var radius = window.innerHeight / 2 - 150;
        var initialAngle = 50;

        var circle = new Konva.Circle({
            x: this.stage.width() / 2,
            y: this.stage.height() / 2,
            radius: radius,
            fill: 'white',
            stroke: 'black',
            strokeWidth: 4
        });

        var anims: any[] = [];

        this.layer.add(circle);
        var cnt = 0;
        for (let state of this.networkstate) {
            var angle = (cnt / this.networkstate.length) * Math.PI;
            var x = radius * Math.cos(angle) + (this.stage.attrs.width / 2);
            var y = radius * Math.sin(angle) + (this.stage.attrs.height / 2);

            var rect = new Konva.Rect({
                x: x,
                y: y,
                width: 50,
                height: 50,
                fill: 'blue',
                stroke: 'black',
                strokeWidth: 4,
                id: state.address
            });

            this.layer.add(rect);

            var diagonal = 50 / Math.SQRT2;
            var radius2 = diagonal + 30;
            var group2 = new Konva.Group({
                draggable: true,
                id: "group-" + state.address 
            });

            group2.add(rect);

            var cnt2 = 0
            for (let agent of state.published_data) {
                var angle = (cnt2 / state.published_data.length) * Math.PI;
                var x = radius2 * Math.cos(angle) + (50 / 2);
                var y = radius2 * Math.sin(angle) + (50 / 2);

                var childRect = new Konva.Rect({
                    x: rect.attrs.x + (rect.attrs.width / 2), //find center point of blue rect
                    y: rect.attrs.y + (rect.attrs.height / 2),
                    width: 30,
                    height: 30,
                    fill: 'yellow',
                    stroke: 'black',
                    strokeWidth: 4,
                    offset: {
                        x: x,
                        y: y + angle
                    },
                    id: "child-" + agent.name
                });

                cnt2 = cnt2 + 1;

                group2.add(childRect);
                

 

                anims.push(childRect);
    
            }
            this.layer.add(group2);
            cnt = cnt + 1
        }

        this.stage.add(this.layer);

        var angularSpeed = 20;
        var anim = new Konva.Animation(function (frame) {
            var angleDiff = frame.timeDiff * angularSpeed / 1000;
            for(let r of anims)
            {
                r.rotate(angleDiff);
            }
            
        }, this.layer);

        anim.start()
    }

    init_old(): void {
        var width = window.innerWidth;
        var height = window.innerHeight;

        var stage = new Konva.Stage({
            container: 'container',
            width: width,
            height: height
        });

        var layer = new Konva.Layer();

        /*
        * leave center point positioned
        * at the default which is the top left
        * corner of the rectangle
        */

        var group = new Konva.Group({
            draggable: true
        });

        var blueRect = new Konva.Rect({
            x: 200,
            y: 100,
            width: 50,
            height: 50,
            fill: '#00D2FF',
            stroke: 'black',
            strokeWidth: 4,
            draggable: true
        });

        var redRect = new Konva.Rect({
            x: 700,
            y: 100,
            width: 50,
            height: 50,
            fill: 'red',
            stroke: 'black',
            strokeWidth: 4,
            draggable: true
        });

        /*
        * move center point to the center
        * of the rectangle with offset
        */

        var yellowRect = new Konva.Rect({
            x: blueRect.attrs.x + (blueRect.attrs.width / 2), //find center point of blue rect
            y: blueRect.attrs.y + (blueRect.attrs.height / 2),
            width: 30,
            height: 30,
            fill: 'yellow',
            stroke: 'black',
            strokeWidth: 4,
            offset: {
                x: blueRect.attrs.width,
                y: blueRect.attrs.height + 50
            }
        });

        var greenRect = new Konva.Rect({
            x: blueRect.attrs.x + (blueRect.attrs.width / 2), //find center point of blue rect
            y: blueRect.attrs.y + (blueRect.attrs.height / 2),
            width: 30,
            height: 30,
            fill: 'green',
            stroke: 'black',
            strokeWidth: 4,
            offset: {
                x: blueRect.attrs.width + 60,
                y: blueRect.attrs.height
            }
        });

        var line = new Konva.Line({
            points: [blueRect.attrs.x + 50, blueRect.attrs.y + 25, redRect.attrs.x, redRect.attrs.y + 25],
            stroke: 'black',
            strokeWidth: 2,
            lineCap: 'round',
            lineJoin: 'round',
        })

        var group2 = new Konva.Group({
            draggable: true
        });


        group.add(blueRect);
        group.add(yellowRect);
        group.add(greenRect);

        group2.add(line)
        group2.add(redRect)
        //group2.add(group);

        layer.add(group);
        layer.add(group2);
        stage.add(layer);

        redRect.on('dragmove', adjustPoint);
        blueRect.on('dragmove', adjustPoint);
        blueRect.on('dragmove', adjustGroup);

        var angularSpeed = 90;
        var anim = new Konva.Animation(function (frame) {
            var angleDiff = frame.timeDiff * angularSpeed / 1000;
            greenRect.rotate(angleDiff);
            yellowRect.rotate(angleDiff);
        }, layer);

        anim.start();

        // one revolution per 4 seconds

        function adjustPoint(e) {
            var p = [blueRect.getX() + 50, blueRect.attrs.y + 25, redRect.attrs.x, redRect.attrs.y + 25];
            line.setPoints(p);
            stage.draw();
        }

        function adjustGroup(e) {
            yellowRect.attrs.x = blueRect.attrs.x + (blueRect.attrs.width / 2); //find center point of blue rect
            yellowRect.attrs.y = blueRect.attrs.y + (blueRect.attrs.height / 2);
            greenRect.attrs.x = blueRect.attrs.x + (blueRect.attrs.width / 2); //find center point of blue rect
            greenRect.attrs.y = blueRect.attrs.y + (blueRect.attrs.height / 2);
            stage.draw();
        }

    }
}
