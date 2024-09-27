function call_gemini(prompt, temperature = 0, tools, image = false, model = "gemini-1.5-flash") {
    const payload =
        {
            "contents":
                [
                    {
                        "parts":
                            [
                                {
                                    "text": prompt
                                }
                            ]
                    }
                ],
            "tools": tools,
            "generationConfig":
                {
                    "temperature": temperature,
                },
        };

    if (image) {
        const imageData = Utilities.base64Encode(image.getAs('image/png').getBytes());
        payload.contents[0].parts.push(
            {
                "inlineData":
                    {
                        "mimeType": "image/png",
                        "data": imageData
                    }
            }
        );
    }

    const options =
        {
            'method': 'post',
            'contentType': 'application/json',
            'payload': JSON.stringify(payload)
        };

    let endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${model}-latest:generateContent?key=${geminiApiKey}`;
    const response = UrlFetchApp.fetch(endpoint, options);
    const data = JSON.parse(response);
    const content = data["candidates"][0]["content"]["parts"][0]["text"];
    return content;
};