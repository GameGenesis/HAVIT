import { Typography } from "@material-tailwind/react";

export default function Tech() {
    return (
        <div className="home">
            <Typography
                as="big"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    <b>Powered by DALL-E 2.</b>
            </Typography>

            <br />
            <br />

            <Typography
                as="big"
                variant="h1"
                className="text-3xl mr-4 cursor-pointer py-1.5 font-normal">
                    <b>What is DALL-E 2?</b>
                    <br />
                    <p>It is a powerful AI image generating tool developed by OpenAI.</p>
                    <br />
                    <b>What is DALL-E 2 used for?</b>
                    <br />
                    <p>It is used to add new artistic figures or elements to the existing image that the user has taken or uploaded to our Firebase cloud storage. Keep in mind that all of this process is done through securely with end-to-end encryption enabled.</p>
            </Typography>
        </div>
    );
}