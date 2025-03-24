using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Extensions.Logging;
using System.Text.RegularExpressions;

namespace docai
{
    public class removeHTMLFromEmailBody
    {
        private readonly ILogger<removeHTMLFromEmailBody> _logger;

        public removeHTMLFromEmailBody(ILogger<removeHTMLFromEmailBody> logger)
        {
            _logger = logger;
        }
 
        [Function("removeHTMLFromEmailBody")]
        public async Task<IActionResult> Run([HttpTrigger(AuthorizationLevel.Anonymous, "get", "post")] HttpRequest req)
        {
            _logger.LogInformation("removeHTMLFromEmailBody: C# function triggered");
            // Parse query parameter
            string emailBodyContent = await new StreamReader(req.Body).ReadToEndAsync();
            // Replace html tags
            string updatedBody = Regex.Replace(emailBodyContent, "<.*?>", string.Empty);
            // Replace comments between <!-- and -->
            updatedBody = Regex.Replace(updatedBody, @"(<!--)(.+?)(-->)", string.Empty, RegexOptions.Singleline);
            updatedBody = updatedBody.Replace("\\r\\n", " ");
            updatedBody = updatedBody.Replace(@"&nbsp;", " ");
            // Replace all whitespaces before and after the cleaned body
            return new OkObjectResult(updatedBody.Trim());
        }
    }
}
