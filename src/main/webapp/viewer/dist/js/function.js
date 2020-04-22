(function ($, window) {
    $.fn.contextMenu = function (settings) {
        return this.each(function () {
            $(this).on("contextmenu", function (e) {
                if (e.ctrlKey) return;
                $(settings.menuSelector)
                    .data("invokedOn", $(e.target))
                    .show()
                    .css({
                        position: "absolute",
                        left: getLeftLocation(e),
                        top: getTopLocation(e)
                    })
                    .off('click')
                    .on('click', function (e) {
                        $(this).hide();
                
                        var $invokedOn = $(this).data("invokedOn");
                        var $selectedMenu = $(e.target);
                        
                        settings.menuSelected.call(this, $invokedOn, $selectedMenu);
                });
                return false;
            });
            $(document).click(function () {
                $(settings.menuSelector).hide();
            });
        });

        function getLeftLocation(e) {
            var mouseWidth = e.pageX;
            var pageWidth = $(window).width();
            var menuWidth = $(settings.menuSelector).width();
            if (mouseWidth + menuWidth > pageWidth &&
                menuWidth < mouseWidth) {
                return mouseWidth - menuWidth;
            } 
            return mouseWidth;
        }        
        
        function getTopLocation(e) {
            var mouseHeight = e.pageY;
            var pageHeight = $(window).height();
            var menuHeight = $(settings.menuSelector).height();
            if (mouseHeight + menuHeight > pageHeight &&
                menuHeight < mouseHeight) {
                return mouseHeight - menuHeight;
            } 
            return mouseHeight;
        }
    };
})(jQuery, window);

function getContextPath() {
	return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}

function getLayer(layer, key, value) {
	if (layer.get(key) === value) {
		return layer;
	}

	if (layer.getLayers) {
		var layers = layer.getLayers().getArray(),
		len = layers.length, result;
		for (var i = 0; i < len; i++) {
			result = getLayer(layers[i], key, value);
			if (result) {
				return result;
			}
		}
	}

	return null;
}

function getLayerByName(name) {
    var layers = map.getLayers();
    var length = layers.getLength();
    for (var i = 0; i < length; i++) {
        if (name === layers.item(i).get('name')) {
            return layers.item(i);
        }
    }
    return null;
}

function addWMTSLayer(name) {
	var projection = ol.proj.get('EPSG:3857');
	var projectionExtent = projection.getExtent();
	var size = ol.extent.getWidth(projectionExtent) / 256;
	var resolutions = new Array(14);
	var matrixIds = new Array(14);
	for (var z = 0; z < 14; ++z) {
	  // generate resolutions and matrixIds arrays for this WMTS
	  resolutions[z] = size / Math.pow(2, z);
	  matrixIds[z] = z;
	}

//	var _target = $('#'+target); 
//	var _tmp = '<li><a href="#"><label><input type="checkbox" aria-label="'+name+'" class="polaris"/></label> '+name+' <i class="fa fa-angle-left pull-right"></i></a>' +
//					'<ul class="treeview-menu">' +
//						'<li><input type="text" style="width: 80%" aria-label="'+name+'" value="" class="slider form-control" data-slider-min="0" data-slider-max="100" data-slider-step="1" data-slider-value="100" data-slider-orientation="horizontal" data-slider-selection="before" data-slider-tooltip="hide" data-slider-id="blue"></li>' +
//					'</ul>' +
//				'</li>';
// 
//	_target.append(_tmp);
	var layer = new ol.layer.Tile({
		opacity: 1.0,
		source: new ol.source.WMTS({
//			url: SERVER_URL + '/services/wmts?',
			url: 'http://14.35.204.242:8080/ws/services/wmts?',
			layer: 'cite',
			//matrixSet: 'googlecrs84quad',
			matrixSet: 'googlemapscompatible', //ok
			//matrixSet: 'InspireCrs84Quad',
			format: 'image/png',
			projection: ol.proj.get('EPSG:3857'),
			tileGrid: new ol.tilegrid.WMTS({
				origin: ol.extent.getTopLeft(ol.proj.get('EPSG:3857').getExtent()),
				resolutions: resolutions,
				matrixIds: matrixIds
			}),
			style: 'default',
			wrapX: true
		}),
		visible: true,
		name: name
	});
	
	map.addLayer(layer);
}

function addWFSLayer(name) {
//	var _target = $('#'+target); 
//	var _tmp = '<li><a href="#"><label><input type="checkbox" aria-label="'+name+'" class="polaris"/></label> '+name+' <i class="fa fa-angle-left pull-right"></i></a>' +
//					'<ul class="treeview-menu">' +
//						'<li><input type="text" style="width: 80%" aria-label="'+name+'" value="" class="slider form-control" data-slider-min="0" data-slider-max="100" data-slider-step="1" data-slider-value="100" data-slider-orientation="horizontal" data-slider-selection="before" data-slider-tooltip="hide" data-slider-id="blue"></li>' +
//					'</ul>' +
//				'</li>';
// 
//	_target.append(_tmp);
	
    var vectorSource = new ol.source.ServerVector({
        format: new ol.format.WFS({
            featureNS: 'http://www.deegree.org/app',
            featureType: 'river'
        }),
        loader: function(extent, resolution, projection) {
//        var epsg4326Extent = ol.proj.transformExtent(extent, projection, 'EPSG:4326');

          var url = SERVER_URL + encodeURIComponent('/services/wfs110?'+
          'service=WFS&request=GetFeature&'+
          'version=1.1.0&typename=app:river&'+
          'srsname=EPSG:3857&'+
          'bbox=' + extent.join(','));
//        'bbox=' + epsg4326Extent.join(',');
        	
            $.ajax({
                url: url
            })
            .done(function(response) {
                vectorSource.addFeatures(vectorSource.readFeatures(response));
            });
        },
        strategy: ol.loadingstrategy.createTile(new ol.tilegrid.XYZ({
            maxZoom: 19
        })),
//        strategy: ol.loadingstrategy.bbox,
        projection: 'EPSG:3857'
    });

    var vectorLayer = new ol.layer.Vector({
        source: vectorSource,
        style: new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'green',
                width: 2
            })
        }),
        visible: true,
        name: name
    });	
    
    
    map.addLayer(vectorLayer);
//  view.setCenter(ol.proj.transform([126.764799, 37.622768], 'EPSG:4326', 'EPSG:3857'));
//	view.setCenter([14135112.735350119, 4511483.060762477]);
//	view.setZoom(15);
}

function getWMSCapabilities(target, callback) {
	var url = getContextPath()+"/services/wms";
	var parser = new ol.format.WMSCapabilities();
	$.ajax(url+"?service=WMS&request=GetCapabilities").then(function(response) {
		var result = parser.read(response);
		var _wmsLegend = $('#'+target); 
		var _tmp = _wmsLegend.html(); 
		var regendURL = "//";
		_wmsLegend.empty();
		$.each(result.Capability.Layer.Layer, function( index, value ) {
//			_wmsLegend.append(_tmp.replace(/\[LEGEND_INDEX\]/g, index).replace(/\[LEGEND_NAME\]/g, value.Name).replace(/\[LEGEND_URL\]/g, value.Style[0].LegendURL[0].OnlineResource));
			try {
				if (typeof value.Style[0].LegendURL !== "undefined") {
					regendURL = value.Style[0].LegendURL[0].OnlineResource;
				}
			} catch(e) {
			
			}
			
			_wmsLegend.append(_tmp.replace(/\[LEGEND_INDEX\]/g, index).replace(/\[LEGEND_NAME\]/g, value.Name).replace(/\/\//g, regendURL));
			var layer = new ol.layer.Image({
				name: value.Name,
				source: new ol.source.ImageWMS({
					url: url,
					ratio: 1.0,
					params: {'LAYERS': value.Name}
				}),
				visible: false
			});
			
//			var layer = new ol.layer.Tile({
//				name: value.Name,
//				source: new ol.source.TileWMS({
//					url: url,
//					params: {'LAYERS': value.Name, 'TILED': true}
//				}),
//				visible: false
//			});
			
			//layer.viewExtent = value.BoundingBox[0].extent;
			map.addLayer(layer);
		});  	  
//		addWMTSLayer(target, "wmtsLayer");
	}).done(function(){
		callback(target);
	});	
}

function buildWMSLegend(target) {
	$("#"+target+" :checkbox").click(function(e) {
		toggleLayer($(e.currentTarget.labels).text());
		zoomToLayer($(e.currentTarget.labels).text());
	});

	$('#'+target+' input[type="text"]').change(function(e) {
		opacityLayer($(e.currentTarget.parentElement.getElementsByTagName("label")).text(), this.value);
	});
}

function toggleLayer(layername) {
//	var layer = getLayer(map.getLayerGroup(), 'name', layername);
	var layer = getLayerByName(layername);
	layer.setVisible(!layer.getVisible());
}


function zoomToLayer(layername) {
//	var layer = getLayer(map.getLayerGroup(), 'name', layername);
	var layer = getLayerByName(layername);
	var extent = ol.proj.transformExtent(layer.viewExtent, 'EPSG:4326', 'EPSG:3857');
	view.fitExtent(extent, map.getSize());
}

function opacityLayer(layername, value) {
//    var layer = getLayer(map.getLayerGroup(), 'name', layername);
	var layer = getLayerByName(layername);
    layer.setOpacity(value);
}

function setBaseLayer(layername) {
	var layer = getLayerByName(layername);
//	for(var i=0; i < map.getLayers().getArray().length; i++) {
//		map.getLayers().getArray()[i].setVisible(false);
//	}
//	layer.setVisible(true);

	for(var i=0; i < layers.length; i++) {
		getLayerByName(layers[i].get("name")).setVisible(false);
	}
	layer.setVisible(true);
	
//    map.getLayers().setAt(map.getLayers().getArray().length, layer);
}

function myLocation() {
	view.setCenter(geolocation.getPosition());	
	view.setZoom(17);
	showPopover();
}

function showPopover() {

//	var element = popup.getElement();
//	var coordinate = evt.coordinate;
	var coordinate = geolocation.getPosition();
	var hdms = ol.coordinate.toStringHDMS(ol.proj.transform(coordinate, 'EPSG:3857', 'EPSG:4326'));

//	$(element).popover('destroy');
	overlay.setPosition(coordinate);
	$(element).popover({
		'placement': 'top',
		'animation': false,
		'html': true,
//		'content': '<p>Spatium 지도에 오신 것을 환영합니다. 당신의 위치:</p><code>' + hdms + '</code>'
		'content': '<p>당신은 여기에 있습니다.</p><code>' + hdms + '</code>'
		
	});
	$(element).popover('toggle');
}

var loadFeatures = function(response) {
	vectorSource.addFeatures(vectorSource.readFeatures(response));
};

function tiltMap() {
	$(".stage *").css("-webkit-transform-style", ($( "#map .ol-viewport:first" ).css("-webkit-transform-style")==="flat")?"preserve-3d":"flat");	
	$( "#map .ol-viewport:first" ).css("overflow", ($( "#map .ol-viewport:first" ).css("overflow")==="hidden")?"visible":"hidden");
	$(".marker").toggle();
	$('.tilter').toggleClass('tiltmap');
	$('.stage').toggleClass('perspective');
	$('.ol-overlay-container').toggleClass('bottomaxis');
	$('.ol-overlay-container').toggleClass('flipmarkers');
	$(".ol-overlaycontainer-stopevent").toggleClass('bottomaxis');
	$(".ol-overlaycontainer-stopevent").toggleClass('flipmarkers');
}

function resizeViewer() {
	$(".content-wrapper").height($(window).height() - $('.navbar').height() - 51);
	$(".map").height($(window).height() - $('.navbar').height() - 51);
	$(".sidebar").height($(window).height() - $('.navbar').height() - 10);
	map.updateSize();
}

function addFeature() {
	var polygonFeature1 = new ol.Feature(
		new ol.geom.Polygon([[[-3e6, -1e6], [-3e6, 1e6], [-1e6, 1e6], [-1e6, -1e6], [-3e6, -1e6]]])
	);

	var polygonFeature2 = new ol.Feature(
		new ol.geom.Polygon([[[-3000000, -1000000], [-3000000, 1000000], [-1000000, 3000000], [-1000000, -3000000], [-3000000, -3000000]]])
	);

	var polygonFeature3 = new ol.Feature(
		new ol.geom.Polygon([[[-3e6, -1e6], [-3e6, 1e6], [-1e6, 1e6], [-1e6, -1e6], [-3e6, -1e6]]])
	);
	
//    var cw = [[-180, -90], [-180, 90], [180, 90], [180, -90], [-180, -90]];
//    var ccw = [[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]];
//	
//	var rightMultiFeature = new ol.Feature(
//			new ol.geom.MultiPolygon([[ccw, cw]])
//	);
//
//	var leftMultiFeature = new ol.Feature(
//			new ol.geom.MultiPolygon([[cw, ccw]])
//	);
	

//    var rightMulti = new ol.geom.MultiPolygon([[ccw, cw]]);
//    var left = new ol.geom.Polygon([cw, ccw]);
//    var leftMulti = new ol.geom.MultiPolygon([[cw, ccw]]);
    
	
	var featureVector = new ol.layer.Vector({
		source: new ol.source.Vector({
//			features: [polygonFeature]
			features: [polygonFeature2]
		}),
		style: new ol.style.Style({
//			image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
//				anchor: [0.5, 46],
//				anchorXUnits: 'fraction',
//				anchorYUnits: 'pixels',
//				opacity: 0.95,
//				src: 'data/icon.png'
//			})),
			stroke: new ol.style.Stroke({
				width: 3,
				color: [255, 0, 0, 1]
			}),
			fill: new ol.style.Fill({
				color: [0, 0, 255, 0.6]
			})
		})
	});	
	
	map.addLayer(featureVector);
}

function zoomToMaxExtent() {
	var view = map.getView();

	var currExtent = view.calculateExtent(map.getSize());
	var maxExtent = ol.proj.transformExtent([120, 33, 135, 40], 'EPSG:4326', 'EPSG:3857');

	if ( ((currExtent[0]<maxExtent[0])&&(currExtent[1]<maxExtent[1]))||((currExtent[2]>maxExtent[2])&&(currExtent[3]>maxExtent[3])) ) {
		view.fitExtent(maxExtent, map.getSize());
	}
}	
