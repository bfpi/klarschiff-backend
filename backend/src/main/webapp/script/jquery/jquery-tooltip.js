ShowTooltip = function(e)
			{
				var text = $(this).next('.show-tooltip-text');
				if (text.attr('class') != 'show-tooltip-text')
					return false;

				text.fadeIn()
					.css('top', e.pageY)
					.css('left', e.pageX+10);

				return false;
			}
			
			HideTooltip = function(e)
			{
				var text = $(this).next('.show-tooltip-text');
				if (text.attr('class') != 'show-tooltip-text')
					return false;

				text.fadeOut();
			}

			SetupTooltips = function()
			{
				$('.show-tooltip')
					.each(function(){
						$(this)
							.after($('<span/>')
								.attr('class', 'show-tooltip-text')
								.html($(this).attr('title')))
							.attr('title', '');
					})
					.hover(ShowTooltip, HideTooltip);
			}

			$(document).ready(function() {
				SetupTooltips();
			});