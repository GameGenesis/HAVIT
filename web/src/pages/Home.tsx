import { Typography } from "@material-tailwind/react";

export default function Home() {
    return (
        <div className="home">
            <Typography
                as="big"
                href="#"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    WELCOME BACK.
            </Typography>

            <br />

            <Typography
                as="big"
                href="#"
                variant="h1"
                className="text-3xl mr-4 cursor-pointer py-1.5 font-normal">
                    Our app brings an unique set of features that combines social media, video editing platform, and a daily habit tracker.
            </Typography>

            <br />

            <button type="button" className="inline-block mt-2 px-6 py-2 border-2 border-gray-800 text-gray-800 text-xs font-bold leading-tight uppercase rounded-full hover:bg-black hover:bg-opacity-5 focus:outline-none focus:ring-0 transition duration-150 ease-in-out">
                <span>Development in Progress</span>
            </button>
            
            <img src="screenshot.png" alt="screenshot" className="mt-5" />
        </div>
    );
}