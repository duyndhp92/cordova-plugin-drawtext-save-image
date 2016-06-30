module.exports = {
	
	// internals
	CLASS: 'JoinImages',
	JOIN_METHOD: 'joinImagesFromData',
	RESIZE_METHOD: 'resizeImageFromData',
	
	join: function(options){
		
		options = options || {};
		
	    // options
		this.ffolder = options.ffolder; //name folder image
		this.ffilename = options.ffilename; //file name
		this.textContent = options.textContent; //text draw in image
		this.firstImage = options.firstImage; // first image data, base64 string (required)		
		this.size = (options.size && options.size > 0) ? options.size : 5; // file output size (MB), default to 5			
		this.successCallback = (options.success && typeof(options.success) === 'function') ? options.success : null; 
		this.errorCallback = (options.error && typeof(options.error) === 'function') ? options.error : null;
				
		if (!this.firstImage) {
		    if (this.errorCallback) {
		        this.errorCallback("Parameters 'firstImage' and 'secondImage' are required.");
		    }
		    return false;
		}
		
		// pass both images and the size
	    //var	args = [this.firstImage, this.secondImage, this.size];
		var args = [this.firstImage, this.size, this.ffolder, this.ffilename, this.textContent];
		
		// make the call
        cordova.exec(this.successCallback, this.errorCallback, this.CLASS, this.JOIN_METHOD, args);
	},
	
	resize: function(options){
		
		options = options || {};
		
		// options
		this.image = options.image; // image data, base64 string (required)
		
		this.size = (options.size && options.size > 0) ? options.size : 5; // file output size (MB), default to 5
		
		// make sure callbacks are functions or reset to null
		this.successCallback = (options.success && typeof(options.success) === 'function') ? options.success : null; 

		this.errorCallback = (options.error && typeof(options.error) === 'function') ? options.error : null;
		
		// make sure the image is set	
		if (!this.image) {
			if (this.errorCallback) {
				this.errorCallback("Parameter 'image' is required.");
			}
			return false;
		}
		
		// pass first image and the size
		var	args = [this.image, this.size];
		
		// make the call
        cordova.exec(this.successCallback, this.errorCallback, this.CLASS, this.RESIZE_METHOD, args);
	}	
};