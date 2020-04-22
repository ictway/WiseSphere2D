$(function(){
	resizeViewer();
	
	$(window).resize(function() {
		resizeViewer();
	});
	
	$( "a.sidebar-toggle" ).click(function() {
		window.setTimeout(function(){
			$(".map").height($(window).height() - $('.navbar').height() - 51);
			$(".sidebar").height($(window).height() - $('.navbar').height() - 10);
			map.updateSize();
		}, 100);
	});	
	
	getWMSCapabilities("wmsLegend", function(target){
		$.AdminLTE.tree(".treeview-menu");
		
		$('#'+target+' input[type="checkbox"].polaris').iCheck({
		    checkboxClass: 'icheckbox_polaris',
		    radioClass: 'iradio_polaris'
// 		    increaseArea: '-10%' // optional			
		}).on('ifToggled', function(e){
			toggleLayer(e.currentTarget.getAttribute("aria-label"));
		});

		$('#'+target).contextMenu({
		    menuSelector: "#contextMenu",
		    menuSelected: function (invokedOn, selectedMenu) {
		        zoomToLayer(invokedOn.text().trim());
		    }
		});		
		
        /* BOOTSTRAP SLIDER */
    	$.each($('#'+target+' .slider'), function( index, value ) {
    		$(this).slider().on('slide', function(e){
    			opacityLayer($(this).closest('li').find("input").attr("aria-label"), e.value !== undefined ? parseInt(e.value) / 100 : 0 );
    		});		
    	});    	
    	
	});

	$("#myLocation").click(function(){
		geolocation.getPosition() === undefined ? geolocation.setTracking(true) : (geolocation.setTracking(false), myLocation());
	});
	
	geolocation.on("change", function() {
		geolocation.setTracking(false);
		myLocation();
	});
	
//	geolocation.on("error", function(error) {
//		console.log(error.message);
//	});

	$("#tiltMap").click(function() {
		tiltMap();
	});

	var angle=80;
	window.setInterval(function(){
		$('.rotating').attr('style','-webkit-transform:rotateZ('+angle+'deg);-moz-transform:rotateZ('+angle+'deg);-moz-transition:-moz-transform 0.75s;');
		if ($('.rotating').length>0) $('.pivotmarker').attr('style','-webkit-transform: rotateY('+angle+'deg);-moz-transform: rotateY('+angle+'deg);-moz-transition:-moz-transform 0.75s;');
		angle=angle*-1;
	},1500);	

	$('input[type="radio"].polaris').iCheck({
	    checkboxClass: 'icheckbox_polaris',
	    radioClass: 'iradio_polaris'
//		    increaseArea: '-10%' // optional			
	}).on('ifChecked', function(e){
		zoomToMaxExtent();
		setBaseLayer(this.id);
		if(this.id === "satelliteLayer") toggleLayer("hybridLayer");
		
	});

	$('#testLayers input[type="checkbox"].polaris').iCheck({
	    checkboxClass: 'icheckbox_polaris',
	    radioClass: 'iradio_polaris'
//		    increaseArea: '-10%' // optional			
	}).on('ifToggled', function(e){
		var _id = e.currentTarget.id;
		if (getLayerByName(_id)) {
			toggleLayer(_id);
		} else {
			if (_id === "wmtsLayer") {
				addWMTSLayer(_id)
			} else if (_id === "wfsLayer") {
				addWFSLayer(_id);
			}
		}
	});
	
//	$("#baseLayers").change(function(e){
//		var idx = parseInt($("#baseLayers").val());
//		for(var i=0; i < layers.length; i++) {
//			map.getLayerGroup().getLayers().getArray()[i].setVisible(false);
//		}
//		map.getLayerGroup().getLayers().getArray()[idx].setVisible(true);
//	});
	
// TTA TEST...BY_JIN --
	map.on('singleclick', function(evt) {
		var params = {'LAYERS' : ''};
		var paramsValue = "";
		$("#wmsLegend input[type=checkbox]:checked").each(function(index, value){
			if(index !== 0) paramsValue += ",";
			paramsValue += $(this).attr("aria-label");
		});
		params.LAYERS = paramsValue; 
		var wmsSource = new ol.source.ImageWMS({
			url: getContextPath()+'/services/wms',
			params: params,
//			serverType: 'geoserver',
			crossOrigin: ''
		});			
		var viewResolution = /** @type {number} */ (view.getResolution());
		var url = wmsSource.getGetFeatureInfoUrl(
				evt.coordinate, viewResolution, 'EPSG:3857',
				{'INFO_FORMAT': 'text/html'});
		if (url) {
//			document.getElementById('info').innerHTML = '<iframe seamless src="' + url + '"></iframe>';
			
			$('#myModal .modal-title').html(paramsValue);
			$('#myModal .modal-body').html('<iframe style="width: 100%; height: 220px; border: 0;" src="' + url + '"></iframe>');
			$('#myModal').modal({
//				remote: url
			});
		}
	});	
//-- TTA TEST...BY_JIN
	
//	map.on('pointermove', function(evt) {
//		if (evt.dragging) {
//			return;
//		}
//		var pixel = map.getEventPixel(evt.originalEvent);
//		var hit = map.forEachLayerAtPixel(pixel, function(layer) {
//			console.log(layer);
//			return true;
//		});
//		map.getTargetElement().style.cursor = hit ? 'pointer' : '';
//	});	
});