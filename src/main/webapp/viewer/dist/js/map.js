var view = new ol.View({
	center : [ 0, 0 ],
	zoom : 2
});

var osmLayer = new ol.layer.Tile({
	source : new ol.source.OSM(),
	name : 'osmLayer'
});

var controls = [ 
	new ol.control.Attribution(),
	new ol.control.OverviewMap({
		className : 'ol-overviewmap ol-custom-overviewmap',
		layers : [ new ol.layer.Tile({
			source : new ol.source.OSM({
				'url' : '//{a-c}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png'
			})
		}) ],
		collapseLabel : '\u00BB',
		label : '\u00AB',
		collapsed : false
	}),	
	new ol.control.Rotate({
		autoHide : false
	}),
	new ol.control.ScaleLine(),
	new ol.control.Zoom(),
//	new ol.control.ZoomSlider(),
	new ol.control.ZoomToExtent(),
	new ol.control.FullScreen()
];

var attributions = [new ol.Attribution({html: 'All maps &copy; <a href="http://map.vworld.kr/">VWORLD</a>'})];

var baseLayer      = new ol.layer.Tile({source: new ol.source.XYZ({attributions: attributions, url: 'http://xdworld.vworld.kr:8080/2d/Base/201411/{z}/{x}/{y}.png'}), visible: false, name: 'baseLayer'});
var grayLayer      = new ol.layer.Tile({source: new ol.source.XYZ({attributions: attributions, url: 'http://xdworld.vworld.kr:8080/2d/gray/201411/{z}/{x}/{y}.png'}), visible: false, name: 'grayLayer'});
var midnightLayer  = new ol.layer.Tile({source: new ol.source.XYZ({attributions: attributions, url: 'http://xdworld.vworld.kr:8080/2d/midnight/201411/{z}/{x}/{y}.png'}), visible: false, name: 'midnightLayer'});
var hybridLayer    = new ol.layer.Tile({source: new ol.source.XYZ({attributions: attributions, url: 'http://xdworld.vworld.kr:8080/2d/Hybrid/201411/{z}/{x}/{y}.png'}), visible: false, name: 'hybridLayer'});
var satelliteLayer = new ol.layer.Tile({source: new ol.source.XYZ({attributions: attributions, url: 'http://xdworld.vworld.kr:8080/2d/Satellite/201301/{z}/{x}/{y}.jpeg'}), visible: false, name: 'satelliteLayer'});

var layers = [ osmLayer, baseLayer, grayLayer, midnightLayer, satelliteLayer, hybridLayer ];

var element = document.getElementById('ol-popover');

var overlay = new ol.Overlay({
  element: element,
  positioning: 'bottom-center',
  stopEvent: false
});

var geolocation = new ol.Geolocation({
	projection: view.getProjection()
});

var map = new ol.Map({
	layers : layers,
	controls : controls,
	renderer : exampleNS.getRendererFromQueryString(),
	target : "map",
	overlays: [overlay],
	view : view
});

